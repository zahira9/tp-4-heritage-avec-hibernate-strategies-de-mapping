package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.entities.Utilisateur;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;
import org.example.service.RapportVentesService.VenteCategorie;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GestionCommercialeTest {
    private final CrudService<Categorie, Long> categorieService = new CrudService<>(Categorie.class);
    private final CrudService<Produit, Long> produitService = new CrudService<>(Produit.class);

    @AfterClass
    public static void closeSessionFactory() {
        HibernateUtil.shutdown();
    }

    @Before
    public void clearDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createQuery("DELETE FROM CommandeProduit").executeUpdate();
                session.createQuery("DELETE FROM Commande").executeUpdate();
                session.createQuery("DELETE FROM Produit").executeUpdate();
                session.createQuery("DELETE FROM Categorie").executeUpdate();
                session.createQuery("DELETE FROM Utilisateur").executeUpdate();
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
    public void genericCrudAndNamedQueriesWorkWithCategoryRelationship() {
        Categorie categorie = categorieService.create(new Categorie("Papeterie"));
        Produit stylo = produitService.create(new Produit("Stylo bleu", new BigDecimal("1.20"), categorie));
        produitService.create(new Produit("Stylo rouge", new BigDecimal("1.50"), categorie));

        assertNotNull(stylo.getId());
        assertEquals(2, categorie.getProduits().size());
        assertEquals(2, new ProduitService().findByCategorie(categorie.getId()).size());
        assertEquals(2, new ProduitService().findByLibelle("stylo").size());
        assertEquals(categorie.getId(), produitService.getById(stylo.getId()).getCategorie().getId());

        stylo.setPrix(new BigDecimal("1.30"));
        produitService.update(stylo);
        assertEquals(1, new ProduitService().search(null, new BigDecimal("1.30"),
                new BigDecimal("1.40"), 1, 10).size());

        produitService.delete(stylo);
        assertEquals(1, produitService.getAll().size());
    }

    @Test
    public void criteriaSearchSupportsOptionalFiltersAndPagination() {
        Categorie categorie = categorieService.create(new Categorie("Informatique"));
        produitService.create(new Produit("Clavier mécanique", new BigDecimal("80.00"), categorie));
        produitService.create(new Produit("Clavier compact", new BigDecimal("45.00"), categorie));
        produitService.create(new Produit("Souris", new BigDecimal("25.00"), categorie));

        List<Produit> firstPage = new ProduitService().search(
                "clavier", new BigDecimal("40.00"), new BigDecimal("90.00"), 1, 1);
        List<Produit> secondPage = new ProduitService().search(
                "clavier", new BigDecimal("40.00"), new BigDecimal("90.00"), 2, 1);

        assertEquals(1, firstPage.size());
        assertEquals(1, secondPage.size());
        assertTrue(firstPage.get(0).getLibelle().startsWith("Clavier"));
        assertTrue(secondPage.get(0).getLibelle().startsWith("Clavier"));
        assertTrue(firstPage.get(0).getId() < secondPage.get(0).getId());
    }

    @Test
    public void joinedInheritancePersistsAndLoadsPolymorphically() {
        new CrudService<Client, Long>(Client.class)
                .create(new Client("Client test", LocalDate.of(2026, 1, 10)));
        new CrudService<Fournisseur, Long>(Fournisseur.class)
                .create(new Fournisseur("Fournisseur test", "Rabat"));

        List<String> types = new GenericDAO<>(Utilisateur.class).findAll().stream()
                .map(user -> user.getClass().getSimpleName())
                .sorted()
                .collect(Collectors.toList());

        assertEquals(2, types.size());
        assertEquals("Client", types.get(0));
        assertEquals("Fournisseur", types.get(1));
    }

    @Test
    public void monthlySalesReportAggregatesPriceTimesQuantityByCategory() {
        Categorie categorie = categorieService.create(new Categorie("Papeterie"));
        Produit stylo = produitService.create(new Produit("Stylo", new BigDecimal("1.20"), categorie));
        Produit crayon = produitService.create(new Produit("Crayon", new BigDecimal("0.80"), categorie));

        Commande commandeMoisPrecedent = new Commande(LocalDate.of(2026, 2, 10));
        commandeMoisPrecedent.addProduit(stylo, 3);
        commandeMoisPrecedent.addProduit(crayon, 5);
        new CrudService<>(Commande.class).create(commandeMoisPrecedent);

        Commande commandeHorsPeriode = new Commande(LocalDate.of(2026, 3, 1));
        commandeHorsPeriode.addProduit(stylo, 100);
        new CrudService<>(Commande.class).create(commandeHorsPeriode);

        List<VenteCategorie> report = new RapportVentesService().calculer(
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));

        assertEquals(1, report.size());
        assertEquals("Papeterie", report.get(0).getCategorieNom());
        assertEquals(0, new BigDecimal("7.60").compareTo(report.get(0).getChiffreAffaires()));
    }

    @Test
    public void rejectsInvalidSearchAndLineInputs() {
        try {
            new ProduitService().search(null, new BigDecimal("20"), new BigDecimal("10"), 1, 10);
            fail("Un prix minimum supérieur au prix maximum doit être rejeté.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("minimum"));
        }

        try {
            new Categorie("   ");
            fail("Un nom de catégorie vide doit être rejeté.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("obligatoire"));
        }
    }
}
