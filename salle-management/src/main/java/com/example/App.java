package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;
import com.example.service.SalleServiceImpl;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SallePU");
        try {
            seedData(entityManagerFactory);

            SalleService service = new SalleServiceImpl(new SalleRepositoryImpl(entityManagerFactory));
            LocalDateTime debut = LocalDateTime.of(2026, 11, 16, 10, 0);
            LocalDateTime fin = LocalDateTime.of(2026, 11, 16, 12, 0);

            System.out.println("Salles disponibles entre " + debut + " et " + fin + " :");
            List<Salle> availableRooms = service.getAvailableRooms(debut, fin);
            availableRooms.forEach(App::printRoom);

            System.out.println("\nRecherche multi-critères paginée (nom contient 'a', capacité >= 8) :");
            PaginationResult<Salle> result = service.searchRooms("a", 8, debut, fin, 1, 2);
            result.getItems().forEach(App::printRoom);
            System.out.println("Page " + result.getCurrentPage() + "/" + result.getTotalPages()
                    + " — " + result.getTotalItems() + " résultat(s)");
        } finally {
            entityManagerFactory.close();
        }
    }

    private static void seedData(EntityManagerFactory entityManagerFactory) {
        LocalDateTime day = LocalDateTime.of(2026, 11, 16, 0, 0);
        Salle atlas = new Salle("Atlas", 4);
        atlas.addReservation(new Reservation(day.withHour(10), day.withHour(12)));

        Salle bahia = new Salle("Bahia", 8);
        bahia.addReservation(new Reservation(day.withHour(11), day.withHour(13)));

        Salle cedar = new Salle("Cedar", 12);
        Salle delta = new Salle("Delta", 16);
        delta.addReservation(new Reservation(day.withHour(12), day.withHour(14)));
        Salle oasis = new Salle("Oasis", 10);
        Salle zenith = new Salle("Zenith", 20);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            Arrays.asList(atlas, bahia, cedar, delta, oasis, zenith).forEach(entityManager::persist);
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

    private static void printRoom(Salle salle) {
        System.out.println("- " + salle.getNom() + " (capacité : " + salle.getCapacite() + ")");
    }
}
