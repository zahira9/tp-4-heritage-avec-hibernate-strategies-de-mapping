package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.repository.ReservationRepository;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import com.example.util.MigrationExecutor;
import com.example.util.PerformanceReport;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ReservationCapstoneTest {
    private static final String JDBC_URL =
            "jdbc:h2:mem:reservation_capstone;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";
    private static EntityManagerFactory emf;

    @BeforeClass
    public static void createFactory() {
        emf = Persistence.createEntityManagerFactory("ReservationPU");
    }

    @AfterClass
    public static void closeFactory() {
        emf.close();
    }

    @Before
    public void resetData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Reservation").executeUpdate();
            em.createQuery("DELETE FROM Salle").executeUpdate();
            em.createQuery("DELETE FROM Utilisateur").executeUpdate();
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        emf.getCache().evictAll();
        new MigrationExecutor(JDBC_URL, "sa", "").execute("db/migration/v2_0_add_description.sql");
        new DataInitializer(emf).initialiser();
    }

    @Test
    public void initializesRealisticDataAndMigrationIsIdempotent() throws Exception {
        new MigrationExecutor(JDBC_URL, "sa", "").execute("db/migration/v2_0_add_description.sql");
        EntityManager em = emf.createEntityManager();
        try {
            assertEquals(Long.valueOf(5), em.createQuery("SELECT COUNT(u) FROM Utilisateur u", Long.class)
                    .getSingleResult());
            assertEquals(Long.valueOf(3), em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                    .getSingleResult());
            assertEquals(Long.valueOf(9), em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                    .getSingleResult());
        } finally {
            em.close();
        }
        try (Connection connection = DriverManager.getConnection(JDBC_URL, "sa", "");
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                             "WHERE TABLE_NAME='SALLES' AND COLUMN_NAME='DESCRIPTION'")) {
            assertTrue(result.next());
        }
    }

    @Test
    public void createsReservationAndRejectsOverlappingBooking() {
        ReservationRepository repository = new ReservationRepository(emf);
        ReservationService service = new ReservationService(emf);
        Long utilisateurId = premierUtilisateurId();
        LocalDateTime debut = LocalDateTime.now().plusDays(20).withMinute(0).withSecond(0).withNano(0);
        List<Salle> disponibles = repository.sallesDisponibles(debut, debut.plusHours(2));
        assertEquals(3, disponibles.size());

        Reservation created = service.creerReservation(disponibles.get(0).getId(), utilisateurId,
                debut, debut.plusHours(2));
        assertNotNull(created.getId());
        assertThrows(IllegalStateException.class, () -> service.creerReservation(
                disponibles.get(0).getId(), utilisateurId, debut.plusMinutes(30), debut.plusHours(1)));

        assertEquals(2, repository.sallesDisponibles(debut, debut.plusHours(2)).size());
    }

    @Test
    public void criteriaSearchFiltersAndPaginatesRooms() {
        ReservationRepository repository = new ReservationRepository(emf);

        List<Salle> results = repository.rechercherSalles("salle", 20, null, null, 1, 1);
        assertEquals(1, results.size());
        assertEquals("Salle Rif", results.get(0).getNom());
        assertEquals(1, repository.rechercherSalles("salle", 20, null, null, 2, 1).size());
        assertThrows(IllegalArgumentException.class,
                () -> repository.rechercherSalles(null, null, null, null, 0, 10));
    }

    @Test
    public void joinsAndEntityGraphLoadReservationDetails() {
        ReservationRepository repository = new ReservationRepository(emf);

        List<Reservation> withJoinFetch = repository.listerReservationsAvecDetailsJoinFetch();
        List<Reservation> withGraph = repository.listerReservationsAvecEntityGraph();

        assertEquals(9, withJoinFetch.size());
        assertEquals(9, withGraph.size());
        assertNotNull(withJoinFetch.get(0).getSalle().getNom());
        assertNotNull(withJoinFetch.get(0).getUtilisateur().getEmail());
    }

    @Test
    public void optimisticLockRejectsStaleConcurrentReservationUpdate() {
        EntityManager em = emf.createEntityManager();
        Long reservationId;
        try {
            reservationId = em.createQuery("SELECT MIN(r.id) FROM Reservation r", Long.class).getSingleResult();
        } finally {
            em.close();
        }
        EntityManager first = emf.createEntityManager();
        EntityManager second = emf.createEntityManager();
        EntityTransaction tx1 = first.getTransaction();
        EntityTransaction tx2 = second.getTransaction();
        try {
            tx1.begin();
            tx2.begin();
            Reservation fresh = first.find(Reservation.class, reservationId);
            Reservation stale = second.find(Reservation.class, reservationId);
            fresh.setCreneau(fresh.getDateDebut().plusHours(1), fresh.getDateFin().plusHours(1));
            tx1.commit();
            stale.setCreneau(stale.getDateDebut().plusHours(2), stale.getDateFin().plusHours(2));
            RollbackException conflict = assertThrows(RollbackException.class, tx2::commit);
            assertTrue(hasOptimisticLockCause(conflict));
        } finally {
            if (tx1.isActive()) {
                tx1.rollback();
            }
            if (tx2.isActive()) {
                tx2.rollback();
            }
            first.close();
            second.close();
        }
    }

    @Test
    public void generatesHibernatePerformanceReport() {
        String report = new PerformanceReport(emf).generer();
        assertTrue(report.contains("Cache L2 hits"));
        assertTrue(report.contains("Statements SQL préparés"));
    }

    @Test
    public void rejectsInvalidBookingInterval() {
        EntityManager em = emf.createEntityManager();
        try {
            Salle salle = em.createQuery("SELECT s FROM Salle s ORDER BY s.id", Salle.class)
                    .setMaxResults(1).getSingleResult();
            Utilisateur utilisateur = em.createQuery("SELECT u FROM Utilisateur u ORDER BY u.id", Utilisateur.class)
                    .setMaxResults(1).getSingleResult();
            LocalDateTime now = LocalDateTime.now();
            assertThrows(IllegalArgumentException.class,
                    () -> new Reservation(utilisateur, salle, now, now));
        } finally {
            em.close();
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

    private Long premierUtilisateurId() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT MIN(u.id) FROM Utilisateur u", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
