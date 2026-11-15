package ma.projet;

import ma.projet.beans.Femme;
import ma.projet.beans.Homme;
import ma.projet.beans.Mariage;
import ma.projet.classes.projets.Employe;
import ma.projet.classes.projets.EmployeTache;
import ma.projet.classes.projets.Projet;
import ma.projet.classes.projets.Tache;
import ma.projet.classes.stock.Categorie;
import ma.projet.classes.stock.Commande;
import ma.projet.classes.stock.Produit;
import ma.projet.service.civil.FemmeService;
import ma.projet.service.civil.HommeService;
import ma.projet.service.projets.EmployeService;
import ma.projet.service.projets.ProjetService;
import ma.projet.service.projets.TacheService;
import ma.projet.service.stock.ProduitService;
import ma.projet.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        try {
            runStockExample();
            runProjectsExample();
            runCivilExample();
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void runStockExample() {
        System.out.println("=== Exercice 1 : Gestion de stock ===");
        ma.projet.service.stock.CategorieService categories =
                new ma.projet.service.stock.CategorieService();
        ma.projet.service.stock.CommandeService commandes =
                new ma.projet.service.stock.CommandeService();
        Categorie informatique = categories.create(new Categorie("INFO", "Informatique"));
        Produit es12 = new Produit("ES12", new BigDecimal("120.00"), informatique);
        Produit zr85 = new Produit("ZR85", new BigDecimal("100.00"), informatique);
        Produit ee85 = new Produit("EE85", new BigDecimal("200.00"), informatique);
        ProduitService produits = new ProduitService();
        produits.create(es12);
        produits.create(zr85);
        produits.create(ee85);
        Commande commande = new Commande(LocalDate.of(2013, 3, 14));
        commande.addProduit(es12, 7);
        commande.addProduit(zr85, 14);
        commande.addProduit(ee85, 5);
        commandes.create(commande);

        System.out.println("Produits par catégorie : " + produits.findByCategorie(informatique.getId()).size());
        System.out.println("Produits commandés en mars 2013 : "
                + produits.findCommandesBetween(LocalDate.of(2013, 3, 1),
                LocalDate.of(2013, 3, 31)).size());
        System.out.println("Produits de la commande " + commande.getId() + " :");
        System.out.println("Commande : " + commande.getId() + "  Date : " + commande.getDate());
        System.out.println("Référence   Prix       Quantité");
        commandes.findLignes(commande.getId()).forEach(ligne ->
                System.out.println(ligne.getProduit().getReference() + "       "
                        + ligne.getProduit().getPrix() + " DH  " + ligne.getQuantite()));
        System.out.println("Produits à plus de 100 DH : "
                + produits.findPriceGreaterThan(new BigDecimal("100.00")).size());
    }

    private static void runProjectsExample() {
        System.out.println("\n=== Exercice 2 : Gestion de projets ===");
        Employe chef = new Employe("Amrani", "Sara", "0600000001");
        new ma.projet.service.projets.EmployeService().create(chef);
        Projet projet = new Projet("Gestion de stock", LocalDate.of(2013, 1, 14),
                LocalDate.of(2013, 12, 31), chef);
        new ProjetService().create(projet);
        Tache analyse = new Tache("Analyse", LocalDate.of(2013, 2, 1),
                LocalDate.of(2013, 2, 28), new BigDecimal("1200.00"));
        Tache conception = new Tache("Conception", LocalDate.of(2013, 3, 1),
                LocalDate.of(2013, 3, 31), new BigDecimal("900.00"));
        projet.addTache(analyse);
        projet.addTache(conception);
        new TacheService().create(analyse);
        new TacheService().create(conception);

        ma.projet.service.projets.EmployeTacheService affectations =
                new ma.projet.service.projets.EmployeTacheService();
        affectations.create(new EmployeTache(chef, analyse, LocalDate.of(2013, 2, 10),
                LocalDate.of(2013, 2, 20)));
        affectations.create(new EmployeTache(chef, conception, null, null));

        System.out.println("Projets gérés : " + new EmployeService().findProjetsGeres(chef.getId()).size());
        System.out.println("Tâches planifiées : " + new ProjetService().findTachesPlanifiees(projet.getId()).size());
        System.out.println("Tâches réalisées : " + new EmployeService().findTachesRealisees(chef.getId()).size());
        System.out.println("Tâches à plus de 1000 DH : " + new TacheService().findPrixSuperieurA1000().size());
        for (ProjetService.TacheRealisee realisee : new ProjetService().findTachesRealisees(projet.getId())) {
            System.out.println(realisee.getTache().getNom() + " : "
                    + realisee.getDateDebutReelle() + " - " + realisee.getDateFinReelle());
        }
    }

    private static void runCivilExample() {
        System.out.println("\n=== Exercice 3 : Gestion de l'état civil ===");
        FemmeService femmesService = new FemmeService();
        HommeService hommesService = new HommeService();
        List<Femme> femmes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            femmes.add(femmesService.create(new Femme("Femme" + i, "Test", "061000000" + i,
                    "Rabat", LocalDate.of(1960 + i, 1, 1))));
        }

        List<Homme> hommes = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            hommes.add(hommesService.create(new Homme("Homme" + i, "Test", "062000000" + i,
                    "Casablanca", LocalDate.of(1955 + i, 2, 2))));
        }

        ma.projet.service.civil.MariageService mariages =
                new ma.projet.service.civil.MariageService();
        for (int i = 0; i < 4; i++) {
            mariages.create(new Mariage(hommes.get(0), femmes.get(i),
                    LocalDate.of(1990 + i, 1, 1), null, i + 1));
        }
        mariages.create(new Mariage(hommes.get(1), femmes.get(4),
                LocalDate.of(1995, 6, 1), LocalDate.of(2000, 6, 1), 2));
        mariages.create(new Mariage(hommes.get(2), femmes.get(4),
                LocalDate.of(2001, 6, 1), null, 1));

        System.out.println("Nombre de femmes : " + femmesService.findAll().size());
        System.out.println("Femme la plus âgée : " + femmesService.findPlusAgee());
        System.out.println("Épouses de " + hommes.get(0).getNom() + " : "
                + hommesService.findEpousesEntre(hommes.get(0).getId(),
                LocalDate.of(1990, 1, 1), LocalDate.of(2000, 12, 31)).size());
        System.out.println("Enfants de " + femmes.get(4).getNom() + " (1990-2010) : "
                + femmesService.countEnfantsEntre(femmes.get(4).getId(),
                LocalDate.of(1990, 1, 1), LocalDate.of(2010, 12, 31)));
        System.out.println("Femmes mariées au moins deux fois : "
                + femmesService.findMarieesAuMoinsDeuxFois().size());
        System.out.println("Hommes mariés à quatre femmes (Criteria) : "
                + hommesService.countHommesAvecQuatreEpousesEntre(
                LocalDate.of(1990, 1, 1), LocalDate.of(2000, 12, 31)));
        System.out.println("Détails des mariages de " + hommes.get(0).getNom() + " :");
        for (Mariage mariage : hommesService.findMariagesDetails(hommes.get(0).getId())) {
            System.out.println("- " + mariage.getFemme().getNom() + " "
                    + mariage.getFemme().getPrenom() + " | début " + mariage.getDateDebut()
                    + " | fin " + mariage.getDateFin() + " | enfants " + mariage.getNbrEnfant());
        }
    }
}
