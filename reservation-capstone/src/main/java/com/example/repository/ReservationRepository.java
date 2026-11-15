package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReservationRepository {
    private final EntityManagerFactory emf;

    public ReservationRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Salle> sallesDisponibles(LocalDateTime debut, LocalDateTime fin) {
        validateInterval(debut, fin);
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM Salle s WHERE NOT EXISTS " +
                                    "(SELECT r.id FROM Reservation r WHERE r.salle = s " +
                                    "AND :debut < r.dateFin AND :fin > r.dateDebut) ORDER BY s.nom",
                            Salle.class)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Salle> rechercherSalles(String nom, Integer capaciteMin, LocalDateTime debut,
                                        LocalDateTime fin, int page, int taille) {
        validateInterval(debut, fin);
        if (capaciteMin != null && capaciteMin < 0 || page < 1 || taille < 1) {
            throw new IllegalArgumentException("Capacité, page ou taille invalide.");
        }
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Salle> criteria = cb.createQuery(Salle.class);
            Root<Salle> salle = criteria.from(Salle.class);
            List<Predicate> predicates = new ArrayList<>();
            if (nom != null && !nom.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(salle.get("nom")),
                        "%" + nom.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (capaciteMin != null) {
                predicates.add(cb.ge(salle.<Integer>get("capacite"), capaciteMin));
            }
            if (debut != null) {
                Subquery<Long> conflits = criteria.subquery(Long.class);
                Root<Reservation> reservation = conflits.from(Reservation.class);
                conflits.select(reservation.get("salle").<Long>get("id"))
                        .where(cb.and(cb.lessThan(reservation.<LocalDateTime>get("dateDebut"), fin),
                                cb.greaterThan(reservation.<LocalDateTime>get("dateFin"), debut)));
                predicates.add(cb.not(salle.get("id").in(conflits)));
            }
            criteria.select(salle).where(predicates.toArray(new Predicate[0]))
                    .orderBy(cb.asc(salle.get("nom")), cb.asc(salle.get("id")));
            return em.createQuery(criteria).setFirstResult((page - 1) * taille)
                    .setMaxResults(taille).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> listerReservationsAvecDetailsJoinFetch() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "SELECT r FROM Reservation r JOIN FETCH r.salle JOIN FETCH r.utilisateur ORDER BY r.dateDebut",
                            Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> listerReservationsAvecEntityGraph() {
        EntityManager em = emf.createEntityManager();
        try {
            EntityGraph<?> graph = em.getEntityGraph("Reservation.details");
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r ORDER BY r.dateDebut", Reservation.class);
            query.setHint("javax.persistence.fetchgraph", graph);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    private static void validateInterval(LocalDateTime debut, LocalDateTime fin) {
        if ((debut == null) != (fin == null) || (debut != null && !debut.isBefore(fin))) {
            throw new IllegalArgumentException("Les dates doivent être absentes ensemble ou former un intervalle valide.");
        }
    }
}
