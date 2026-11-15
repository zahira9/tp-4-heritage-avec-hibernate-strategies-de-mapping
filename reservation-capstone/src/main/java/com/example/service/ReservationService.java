package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import org.hibernate.exception.ConstraintViolationException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationService {
    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final EntityManagerFactory emf;

    public ReservationService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Reservation creerReservation(Long salleId, Long utilisateurId,
                                        LocalDateTime debut, LocalDateTime fin) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Salle salle = em.find(Salle.class, salleId, LockModeType.PESSIMISTIC_WRITE);
            Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
            if (salle == null || utilisateur == null) {
                throw new IllegalArgumentException("La salle ou l'utilisateur demandé n'existe pas.");
            }
            Long conflits = em.createQuery(
                            "SELECT COUNT(r) FROM Reservation r WHERE r.salle.id = :salleId " +
                                    "AND :debut < r.dateFin AND :fin > r.dateDebut", Long.class)
                    .setParameter("salleId", salleId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getSingleResult();
            if (conflits > 0) {
                throw new IllegalStateException("La salle est déjà réservée sur ce créneau.");
            }
            Reservation reservation = new Reservation(utilisateur, salle, debut, fin);
            em.persist(reservation);
            tx.commit();
            LOGGER.info("Réservation créée : id=" + reservation.getId() + ", salle=" + salle.getNom());
            return reservation;
        } catch (RuntimeException e) {
            rollback(tx);
            LOGGER.log(Level.SEVERE, "Échec de création de réservation", e);
            if (hasCause(e, OptimisticLockException.class)) {
                throw new IllegalStateException("Conflit de concurrence, veuillez réessayer.", e);
            }
            if (hasCause(e, ConstraintViolationException.class)) {
                throw new IllegalStateException("Une contrainte d'intégrité empêche cette réservation.", e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static void rollback(EntityTransaction tx) {
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    private static boolean hasCause(Throwable error, Class<? extends Throwable> type) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (type.isInstance(cause)) {
                return true;
            }
        }
        return false;
    }
}
