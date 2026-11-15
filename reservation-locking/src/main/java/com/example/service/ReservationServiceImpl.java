package com.example.service;

import com.example.model.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {
    private final EntityManagerFactory emf;

    public ReservationServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Reservation creerReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(reservation);
            transaction.commit();
            return reservation;
        } catch (RuntimeException e) {
            rollback(transaction);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Reservation managed = em.merge(reservation);
            transaction.commit();
            return managed;
        } catch (OptimisticLockException e) {
            rollback(transaction);
            throw e;
        } catch (RuntimeException e) {
            rollback(transaction);
            if (isOptimisticLockFailure(e)) {
                throw new OptimisticLockException("Une réservation a été modifiée par une autre transaction.", e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r ORDER BY r.id", Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    private static void rollback(EntityTransaction transaction) {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }

    private static boolean isOptimisticLockFailure(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof OptimisticLockException) {
                return true;
            }
        }
        return false;
    }
}
