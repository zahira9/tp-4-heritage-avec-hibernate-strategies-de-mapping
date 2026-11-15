package org.example.service;

import org.example.entities.Produit;
import org.example.util.HibernateUtil;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProduitService {
    public List<Produit> findByCategorie(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("L'identifiant de catégorie est obligatoire.");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNamedQuery("Produit.findByCategorie", Produit.class)
                    .setParameter("catId", categoryId)
                    .getResultList();
        }
    }

    public List<Produit> findByLibelle(String term) {
        if (term == null || term.trim().isEmpty()) {
            throw new IllegalArgumentException("Le terme de recherche est obligatoire.");
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNamedQuery("Produit.searchByLibelle", Produit.class)
                    .setParameter("term", term.trim())
                    .getResultList();
        }
    }

    public List<Produit> search(String term, BigDecimal minPrice, BigDecimal maxPrice,
                                int page, int pageSize) {
        validateSearch(minPrice, maxPrice, page, pageSize);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Produit> criteria = builder.createQuery(Produit.class);
            Root<Produit> produit = criteria.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.trim().isEmpty()) {
                predicates.add(builder.like(builder.lower(produit.get("libelle")),
                        "%" + term.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(produit.<BigDecimal>get("prix"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(produit.<BigDecimal>get("prix"), maxPrice));
            }

            criteria.select(produit)
                    .where(predicates.toArray(new Predicate[0]))
                    .orderBy(builder.asc(produit.get("id")));
            return session.createQuery(criteria)
                    .setFirstResult((page - 1) * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
    }

    private void validateSearch(BigDecimal minPrice, BigDecimal maxPrice, int page, int pageSize) {
        if (minPrice != null && minPrice.signum() < 0
                || maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("Les prix de recherche ne peuvent pas être négatifs.");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Le prix minimum ne peut dépasser le prix maximum.");
        }
        if (page < 1 || pageSize < 1 || (long) (page - 1) * pageSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Les paramètres de pagination sont invalides.");
        }
    }
}
