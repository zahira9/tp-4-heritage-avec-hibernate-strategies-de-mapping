package org.example;

import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;
import org.example.service.RapportVentesService.VenteCategorie;
import org.example.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class App {
    public static void main(String[] args) {
        try {
            CrudService<Categorie, Long> categories = new CrudService<>(Categorie.class);
            CrudService<Produit, Long> produits = new CrudService<>(Produit.class);
            CrudService<Commande, Long> commandes = new CrudService<>(Commande.class);
            CrudService<Client, Long> clients = new CrudService<>(Client.class);
            CrudService<Fournisseur, Long> fournisseurs = new CrudService<>(Fournisseur.class);

            Categorie papeterie = categories.create(new Categorie("Papeterie"));
            Categorie informatique = categories.create(new Categorie("Informatique"));
            Produit stylo = produits.create(new Produit("Stylo", new BigDecimal("1.20"), papeterie));
            Produit ordinateur = produits.create(
                    new Produit("Ordinateur portable", new BigDecimal("950.00"), informatique));

            YearMonth dernierMois = YearMonth.now().minusMonths(1);
            Commande commande = new Commande(dernierMois.atDay(10));
            commande.addProduit(stylo, 3);
            commande.addProduit(ordinateur, 2);
            commandes.create(commande);

            clients.create(new Client("Amine", LocalDate.now().minusMonths(3)));
            fournisseurs.create(new Fournisseur("Fournisseur Atlas", "Casablanca"));

            System.out.println("Produits trouvés par Named Query : "
                    + new ProduitService().findByCategorie(papeterie.getId()).size());
            System.out.println("Produits paginés (prix >= 1.00) : "
                    + new ProduitService().search(null, new BigDecimal("1.00"),
                    null, 1, 10).size());

            System.out.println("Rapport du mois précédent :");
            for (VenteCategorie vente : new RapportVentesService().calculer(
                    dernierMois.atDay(1), dernierMois.plusMonths(1).atDay(1))) {
                System.out.println("- " + vente.getCategorieNom() + " : "
                        + vente.getChiffreAffaires().toPlainString());
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
