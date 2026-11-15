package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.ReservationRepository;
import com.example.service.ReservationService;
import com.example.util.DataInitializer;
import com.example.util.MigrationExecutor;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class MainApp {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final String JDBC_URL =
            "jdbc:h2:mem:reservation_capstone;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000";

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("ReservationPU");
        try {
            new MigrationExecutor(JDBC_URL, "sa", "")
                    .execute("db/migration/v2_0_add_description.sql");
            new DataInitializer(emf).initialiser();

            ReservationRepository repository = new ReservationRepository(emf);
            ReservationService service = new ReservationService(emf);
            LocalDateTime debut = LocalDateTime.now().plusDays(10).withMinute(0).withSecond(0).withNano(0);
            List<Salle> libres = repository.sallesDisponibles(debut, debut.plusHours(2));
            System.out.println("Salles disponibles : " + libres.size());

            if (!libres.isEmpty()) {
                EntityManager em = emf.createEntityManager();
                Long utilisateurId;
                try {
                    utilisateurId = em.createQuery("SELECT MIN(u.id) FROM Utilisateur u", Long.class)
                            .getSingleResult();
                } finally {
                    em.close();
                }
                Reservation reservation = service.creerReservation(libres.get(0).getId(), utilisateurId,
                        debut, debut.plusHours(2));
                System.out.println("Nouvelle réservation créée : " + reservation.getId());
            }

            List<Reservation> reservations = repository.listerReservationsAvecDetailsJoinFetch();
            System.out.println("Réservations chargées avec JOIN FETCH : " + reservations.size());
            System.out.println("Réservations chargées avec Entity Graph : "
                    + repository.listerReservationsAvecEntityGraph().size());

            System.out.println(new PerformanceReport(emf).generer());
        } catch (RuntimeException e) {
            LOGGER.severe("Échec du lancement de l'application : " + e.getMessage());
            throw e;
        } finally {
            emf.close();
        }
    }
}
