package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public class ConcurrentReservationSimulator {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("reservationPU");
        try {
            ReservationService service = new ReservationServiceImpl(emf);
            LocalDateTime debut = LocalDateTime.now().plusDays(1);
            Reservation initiale = service.creerReservation(
                    new Reservation("Salle A", debut, debut.plusHours(2)));

            EntityManager em1 = emf.createEntityManager();
            EntityManager em2 = emf.createEntityManager();
            EntityTransaction tx1 = em1.getTransaction();
            EntityTransaction tx2 = em2.getTransaction();
            try {
                tx1.begin();
                tx2.begin();
                Reservation reservation1 = em1.find(Reservation.class, initiale.getId());
                Reservation reservation2 = em2.find(Reservation.class, initiale.getId());

                reservation1.setSalle("Salle B");
                tx1.commit();
                System.out.println("Transaction 1 : modification réussie (version "
                        + reservation1.getVersion() + ").");

                reservation2.setSalle("Salle C");
                try {
                    tx2.commit();
                    throw new IllegalStateException("Le conflit optimiste attendu ne s'est pas produit.");
                } catch (RuntimeException e) {
                    if (!hasOptimisticLockCause(e)) {
                        throw e;
                    }
                    System.out.println("Transaction 2 : conflit détecté, mise à jour obsolète refusée.");
                }

                Reservation stale = service.trouverParId(initiale.getId());
                stale.setSalle("Salle C");
                Reservation updated = new ReservationRetryHandler(service).modifierAvecRetry(stale);
                System.out.println("Après rechargement, la réservation est en " + updated.getSalle()
                        + " (version " + updated.getVersion() + ").");
            } finally {
                if (tx1.isActive()) {
                    tx1.rollback();
                }
                if (tx2.isActive()) {
                    tx2.rollback();
                }
                em1.close();
                em2.close();
            }
        } finally {
            emf.close();
        }
    }

    private static boolean hasOptimisticLockCause(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof OptimisticLockException) {
                return true;
            }
        }
        return false;
    }
}
