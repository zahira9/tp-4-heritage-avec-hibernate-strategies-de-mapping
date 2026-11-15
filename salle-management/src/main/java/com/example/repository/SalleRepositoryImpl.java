package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;

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

public class SalleRepositoryImpl implements SalleRepository {
    private final EntityManagerFactory entityManagerFactory;

    public SalleRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        if (entityManagerFactory == null) {
            throw new IllegalArgumentException("L'EntityManagerFactory est obligatoire.");
        }
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        validateTimeRange(debut, fin);
        String jpql = "SELECT s FROM Salle s WHERE s.id NOT IN (" +
                "SELECT r.salle.id FROM Reservation r " +
                "WHERE :debut < r.fin AND :fin > r.debut) " +
                "ORDER BY s.nom, s.id";
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TypedQuery<Salle> query = entityManager.createQuery(jpql, Salle.class);
            return query.setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut,
                                   LocalDateTime fin, int page, int size) {
        validateSearch(nom, capaciteMin, debut, fin, page, size);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Salle> criteria = builder.createQuery(Salle.class);
            Root<Salle> salle = criteria.from(Salle.class);
            criteria.select(salle)
                    .where(buildPredicates(builder, criteria, salle, nom, capaciteMin, debut, fin)
                            .toArray(new Predicate[0]))
                    .orderBy(builder.asc(salle.get("nom")), builder.asc(salle.get("id")));

            return entityManager.createQuery(criteria)
                    .setFirstResult((page - 1) * size)
                    .setMaxResults(size)
                    .getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        validateSearch(nom, capaciteMin, debut, fin, 1, 1);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
            Root<Salle> salle = criteria.from(Salle.class);
            criteria.select(builder.count(salle))
                    .where(buildPredicates(builder, criteria, salle, nom, capaciteMin, debut, fin)
                            .toArray(new Predicate[0]));
            return entityManager.createQuery(criteria).getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    private List<Predicate> buildPredicates(CriteriaBuilder builder, CriteriaQuery<?> criteria,
                                            Root<Salle> salle, String nom, Integer capaciteMin,
                                            LocalDateTime debut, LocalDateTime fin) {
        List<Predicate> predicates = new ArrayList<>();
        if (nom != null && !nom.trim().isEmpty()) {
            predicates.add(builder.like(builder.lower(salle.get("nom")),
                    "%" + nom.trim().toLowerCase(Locale.ROOT) + "%"));
        }
        if (capaciteMin != null) {
            predicates.add(builder.ge(salle.<Integer>get("capacite"), capaciteMin));
        }
        if (debut != null) {
            Subquery<Long> reservedRoomIds = criteria.subquery(Long.class);
            Root<Reservation> reservation = reservedRoomIds.from(Reservation.class);
            reservedRoomIds.select(reservation.get("salle").<Long>get("id"))
                    .where(builder.and(
                            builder.lessThan(reservation.<LocalDateTime>get("debut"), fin),
                            builder.greaterThan(reservation.<LocalDateTime>get("fin"), debut)));
            predicates.add(builder.not(salle.get("id").in(reservedRoomIds)));
        }
        return predicates;
    }

    private void validateSearch(String nom, Integer capaciteMin, LocalDateTime debut,
                                LocalDateTime fin, int page, int size) {
        validateTimeRange(debut, fin);
        if (capaciteMin != null && capaciteMin < 0) {
            throw new IllegalArgumentException("La capacité minimale ne peut pas être négative.");
        }
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("La page et la taille doivent être supérieures à zéro.");
        }
        if ((long) (page - 1) * size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("La page demandée dépasse la limite de pagination.");
        }
    }

    private void validateTimeRange(LocalDateTime debut, LocalDateTime fin) {
        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException("Les dates de début et de fin doivent être fournies ensemble.");
        }
        if (debut != null && !debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de début doit précéder la date de fin.");
        }
    }
}
