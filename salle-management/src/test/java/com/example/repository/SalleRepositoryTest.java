package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SalleRepositoryTest {
    private static EntityManagerFactory entityManagerFactory;
    private SalleService service;

    @BeforeClass
    public static void createEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("SallePU");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @Before
    public void setUp() {
        service = new SalleServiceImpl(new SalleRepositoryImpl(entityManagerFactory));
        LocalDateTime day = LocalDateTime.of(2026, 11, 16, 0, 0);
        Salle atlas = new Salle("Atlas", 4);
        atlas.addReservation(new Reservation(day.withHour(10), day.withHour(12)));

        Salle bahia = new Salle("Bahia", 8);
        bahia.addReservation(new Reservation(day.withHour(11), day.withHour(13)));

        Salle cedar = new Salle("Cedar", 12);
        Salle delta = new Salle("Delta", 16);
        delta.addReservation(new Reservation(day.withHour(12), day.withHour(14)));

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Reservation").executeUpdate();
            entityManager.createQuery("DELETE FROM Salle").executeUpdate();
            Arrays.asList(atlas, bahia, cedar, delta).forEach(entityManager::persist);
            entityManager.getTransaction().commit();
        } catch (RuntimeException exception) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    @Test
    public void findsAvailableRoomsAndAllowsBackToBackReservations() {
        LocalDateTime debut = LocalDateTime.of(2026, 11, 16, 11, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 16, 12, 0);

        List<String> names = service.getAvailableRooms(debut, fin).stream()
                .map(Salle::getNom)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("Cedar", "Delta"), names);
        assertFalse(names.contains("Atlas"));
        assertFalse(names.contains("Bahia"));
    }

    @Test
    public void appliesNameCapacityAndAvailabilityFiltersToPaginatedSearch() {
        LocalDateTime debut = LocalDateTime.of(2026, 11, 16, 11, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 11, 16, 12, 0);

        PaginationResult<Salle> firstPage = service.searchRooms("a", 12, debut, fin, 1, 1);
        PaginationResult<Salle> secondPage = service.searchRooms("a", 12, debut, fin, 2, 1);

        assertEquals(2, firstPage.getTotalItems());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals("Cedar", firstPage.getItems().get(0).getNom());
        assertEquals("Delta", secondPage.getItems().get(0).getNom());
        assertEquals(2, secondPage.getCurrentPage());
    }

    @Test
    public void rejectsIncompleteOrInvalidTimeRanges() {
        try {
            service.getAvailableRooms(LocalDateTime.of(2026, 11, 16, 11, 0), null);
            fail("Une plage de dates incomplète doit être rejetée.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("ensemble"));
        }

        try {
            service.getAvailableRooms(LocalDateTime.of(2026, 11, 16, 12, 0),
                    LocalDateTime.of(2026, 11, 16, 11, 0));
            fail("Une plage de dates inversée doit être rejetée.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("précéder"));
        }
    }

    @Test
    public void rejectsInvalidPagination() {
        try {
            service.searchRooms(null, null, null, null, 0, 10);
            fail("Une page inférieure à 1 doit être rejetée.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("supérieures à zéro"));
        }
    }
}
