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
import ma.projet.service.projets.EmployeTacheService;
import ma.projet.service.projets.ProjetService;
import ma.projet.service.projets.TacheService;
import ma.projet.service.stock.ProduitService;
import ma.projet.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EvaluationIntegrationTest {
    @AfterClass
    public static void closeSessionFactory() {
        HibernateUtil.shutdown();
    }

    @Before
    public void clearDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("DELETE FROM EmployeTache").executeUpdate();
                session.createQuery("DELETE FROM Tache").executeUpdate();
                session.createQuery("DELETE FROM Projet").executeUpdate();
                session.createQuery("DELETE FROM Employe").executeUpdate();
                session.createQuery("DELETE FROM Mariage").executeUpdate();
                session.createQuery("DELETE FROM Femme").executeUpdate();
                session.createQuery("DELETE FROM Homme").executeUpdate();
                session.createQuery("DELETE FROM LigneCommande").executeUpdate();
                session.createQuery("DELETE FROM Commande").executeUpdate();
                session.createQuery("DELETE FROM Produit").executeUpdate();
                session.createQuery("DELETE FROM Categorie").executeUpdate();
                transaction.commit();
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    @Test
    public void stockExerciseCoversCategoryOrdersAndNamedQuery() {
        Categorie categorie = new ma.projet.service.stock.CategorieService()
                .create(new Categorie("INFO", "Informatique"));
        Produit es12 = new Produit("ES12", new BigDecimal("120.00"), categorie);
        Produit zr85 = new Produit("ZR85", new BigDecimal("100.00"), categorie);
        ProduitService produits = new ProduitService();
        produits.create(es12);
        produits.create(zr85);
        Commande commande = new Commande(LocalDate.of(2013, 3, 14));
        commande.addProduit(es12, 7);
        commande.addProduit(zr85, 14);
        new ma.projet.service.stock.CommandeService().create(commande);

        assertEquals(2, produits.findByCategorie(categorie.getId()).size());
        assertEquals(2, produits.findByCommande(commande.getId()).size());
        assertEquals(7, new ma.projet.service.stock.CommandeService()
                .findLignes(commande.getId()).get(0).getQuantite());
        assertEquals(2, produits.findCommandesBetween(LocalDate.of(2013, 3, 1),
                LocalDate.of(2013, 3, 31)).size());
        assertEquals(1, produits.findPriceGreaterThan(new BigDecimal("100")).size());
    }

    @Test
    public void projectsExerciseCoversManagedProjectsAssignmentsAndRealDates() {
        Employe employe = new Employe("Benali", "Sara", "0600000000");
        new EmployeService().create(employe);
        Projet projet = new Projet("Projet test", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), employe);
        new ProjetService().create(projet);
        Tache terminee = new Tache("Analyse", LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28), new BigDecimal("1500"));
        Tache planifiee = new Tache("Conception", LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31), new BigDecimal("900"));
        projet.addTache(terminee);
        projet.addTache(planifiee);
        new TacheService().create(terminee);
        new TacheService().create(planifiee);
        EmployeTacheService affectations = new EmployeTacheService();
        affectations.create(new EmployeTache(employe, terminee,
                LocalDate.of(2026, 2, 5), LocalDate.of(2026, 2, 20)));
        affectations.create(new EmployeTache(employe, planifiee, null, null));

        assertEquals(1, new EmployeService().findProjetsGeres(employe.getId()).size());
        assertEquals(1, new EmployeService().findTachesRealisees(employe.getId()).size());
        assertEquals(2, new ProjetService().findTachesPlanifiees(projet.getId()).size());
        assertEquals(1, new ProjetService().findTachesRealisees(projet.getId()).size());
        assertEquals(1, new TacheService().findPrixSuperieurA1000().size());
        assertEquals(1, new TacheService().findRealiseesBetween(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28)).size());
    }

    @Test
    public void civilStatusExerciseCoversRequiredCountsAndMarriageQueries() {
        FemmeService femmeService = new FemmeService();
        HommeService hommeService = new HommeService();
        List<Femme> femmes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            femmes.add(femmeService.create(new Femme("Femme" + i, "Test", null, "Rabat",
                    LocalDate.of(1960 + i, 1, 1))));
        }
        List<Homme> hommes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hommes.add(hommeService.create(new Homme("Homme" + i, "Test", null, "Rabat",
                    LocalDate.of(1950 + i, 1, 1))));
        }

        ma.projet.service.civil.MariageService mariageService =
                new ma.projet.service.civil.MariageService();
        for (int i = 0; i < 4; i++) {
            mariageService.create(new Mariage(hommes.get(0), femmes.get(i),
                    LocalDate.of(1990 + i, 1, 1), null, i + 1));
        }
        mariageService.create(new Mariage(hommes.get(1), femmes.get(4),
                LocalDate.of(1994, 1, 1), LocalDate.of(1996, 1, 1), 2));
        mariageService.create(new Mariage(hommes.get(2), femmes.get(4),
                LocalDate.of(1998, 1, 1), null, 3));

        assertEquals(10, femmeService.findAll().size());
        assertNotNull(femmeService.findPlusAgee());
        assertEquals(4, hommeService.findEpousesEntre(hommes.get(0).getId(),
                LocalDate.of(1990, 1, 1), LocalDate.of(2000, 12, 31)).size());
        assertEquals(5, femmeService.countEnfantsEntre(femmes.get(4).getId(),
                LocalDate.of(1990, 1, 1), LocalDate.of(2000, 12, 31)));
        assertEquals(1, femmeService.findMarieesAuMoinsDeuxFois().size());
        assertEquals(1, hommeService.countHommesAvecQuatreEpousesEntre(
                LocalDate.of(1990, 1, 1), LocalDate.of(2000, 12, 31)));
        assertEquals(4, hommeService.findMariagesDetails(hommes.get(0).getId()).size());
    }
}
