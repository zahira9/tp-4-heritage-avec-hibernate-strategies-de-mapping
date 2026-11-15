package ma.projet.service.stock;

import ma.projet.classes.stock.Produit;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ProduitService extends GenericDao<Produit, Long> {
    public ProduitService() {
        super(Produit.class);
    }

    public List<Produit> findByCategorie(Long categorieId) {
        if (categorieId == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("SELECT p FROM Produit p WHERE p.categorie.id = :id ORDER BY p.reference",
                            Produit.class)
                    .setParameter("id", categorieId)
                    .getResultList();
        }
    }

    public List<Produit> findCommandesBetween(LocalDate debut, LocalDate fin) {
        validateDates(debut, fin);
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT p FROM Produit p JOIN p.lignes l JOIN l.commande c "
                                    + "WHERE c.date BETWEEN :debut AND :fin ORDER BY p.reference",
                            Produit.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        }
    }

    public List<Produit> findByCommande(Long commandeId) {
        if (commandeId == null) {
            throw new IllegalArgumentException("La commande est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT p FROM Produit p JOIN p.lignes l WHERE l.commande.id = :id "
                                    + "ORDER BY p.reference", Produit.class)
                    .setParameter("id", commandeId)
                    .getResultList();
        }
    }

    public List<Produit> findPriceGreaterThan(BigDecimal prix) {
        if (prix == null || prix.signum() < 0) {
            throw new IllegalArgumentException("Le prix seuil doit être positif ou nul.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createNamedQuery("StockProduit.priceGreaterThan", Produit.class)
                    .setParameter("prix", prix)
                    .getResultList();
        }
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || debut.isAfter(fin)) {
            throw new IllegalArgumentException("La période de recherche est invalide.");
        }
    }
}
