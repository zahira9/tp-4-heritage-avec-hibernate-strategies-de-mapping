package org.example.service;

import org.example.entities.Categorie;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import org.example.util.HibernateUtil;
import org.hibernate.Session;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RapportVentesService {
    public List<VenteCategorie> calculer(LocalDate debutInclus, LocalDate finExclue) {
        if (debutInclus == null || finExclue == null || !debutInclus.isBefore(finExclue)) {
            throw new IllegalArgumentException("La période de rapport est invalide.");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Tuple> criteria = builder.createTupleQuery();
            Root<CommandeProduit> ligne = criteria.from(CommandeProduit.class);
            Join<CommandeProduit, Commande> commande = ligne.join("commande");
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");

            Expression<BigDecimal> montantLigne =
                    builder.prod(produit.<BigDecimal>get("prix"),
                            ligne.get("quantite").as(BigDecimal.class));
            criteria.multiselect(
                            categorie.get("id").alias("categorieId"),
                            categorie.get("nom").alias("categorieNom"),
                            builder.sum(montantLigne).alias("chiffreAffaires"))
                    .where(builder.and(
                            builder.greaterThanOrEqualTo(commande.<LocalDate>get("dateCommande"), debutInclus),
                            builder.lessThan(commande.<LocalDate>get("dateCommande"), finExclue)))
                    .groupBy(categorie.get("id"), categorie.get("nom"))
                    .orderBy(builder.asc(categorie.get("nom")));

            List<Tuple> rows = session.createQuery(criteria).getResultList();
            List<VenteCategorie> result = new ArrayList<>(rows.size());
            for (Tuple row : rows) {
                result.add(new VenteCategorie(
                        row.get("categorieId", Long.class),
                        row.get("categorieNom", String.class),
                        row.get("chiffreAffaires", BigDecimal.class)));
            }
            return result;
        }
    }

    public static final class VenteCategorie {
        private final Long categorieId;
        private final String categorieNom;
        private final BigDecimal chiffreAffaires;

        public VenteCategorie(Long categorieId, String categorieNom, BigDecimal chiffreAffaires) {
            this.categorieId = categorieId;
            this.categorieNom = categorieNom;
            this.chiffreAffaires = chiffreAffaires;
        }

        public Long getCategorieId() {
            return categorieId;
        }

        public String getCategorieNom() {
            return categorieNom;
        }

        public BigDecimal getChiffreAffaires() {
            return chiffreAffaires;
        }
    }
}
