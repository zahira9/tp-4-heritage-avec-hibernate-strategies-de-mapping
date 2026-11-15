package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class ReservationOptimisticLockingTest {
    private static EntityManagerFactory emf;
    private static ReservationService service;

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("reservationPU");
        service = new ReservationServiceImpl(emf);
    }

    @AfterClass
    public static void tearDown() {
        emf.close();
    }

    @Before
    public void clearReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Reservation").executeUpdate();
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    @Test
    public void concurrentStaleUpdateIsRejected() {
        Reservation reservation = creerReservation("Salle A");
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx1 = em1.getTransaction();
        EntityTransaction tx2 = em2.getTransaction();
        try {
            tx1.begin();
            tx2.begin();
            Reservation first = em1.find(Reservation.class, reservation.getId());
            Reservation stale = em2.find(Reservation.class, reservation.getId());
            assertEquals(first.getVersion(), stale.getVersion());

            first.setSalle("Salle B");
            tx1.commit();
            assertEquals(Long.valueOf(1), first.getVersion());

            stale.setSalle("Salle C");
            RollbackException conflict = assertThrows(RollbackException.class, tx2::commit);
            assertOptimisticLockCause(conflict);
            assertEquals("Salle B", service.trouverParId(reservation.getId()).getSalle());
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
    }

    @Test
    public void retryReloadsTheLatestVersionAndAppliesRequestedChange() {
        Reservation reservation = creerReservation("Salle A");
        Reservation stale = service.trouverParId(reservation.getId());
        Reservation winner = service.trouverParId(reservation.getId());
        winner.setSalle("Salle B");
        service.modifierReservation(winner);

        stale.setSalle("Salle C");
        Reservation updated = new ReservationRetryHandler(service).modifierAvecRetry(stale);

        assertEquals("Salle C", updated.getSalle());
        assertEquals(Long.valueOf(2), updated.getVersion());
        assertEquals("Salle C", service.trouverParId(reservation.getId()).getSalle());
    }

    @Test
    public void createAndReadReservation() {
        Reservation reservation = creerReservation("Salle A");
        Reservation loaded = service.trouverParId(reservation.getId());

        assertNotNull(loaded);
        assertEquals("Salle A", loaded.getSalle());
        assertEquals(Long.valueOf(0), loaded.getVersion());
        assertEquals(1, service.listerReservations().size());
    }

    private Reservation creerReservation(String salle) {
        LocalDateTime debut = LocalDateTime.now().plusDays(1);
        return service.creerReservation(new Reservation(salle, debut, debut.plusHours(2)));
    }

    private static void assertOptimisticLockCause(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof OptimisticLockException) {
                return;
            }
        }
        throw new AssertionError("Aucune OptimisticLockException dans la chaîne des causes.", error);
    }
}
