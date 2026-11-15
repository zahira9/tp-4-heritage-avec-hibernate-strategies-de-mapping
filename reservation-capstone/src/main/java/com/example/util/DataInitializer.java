package com.example.util;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataInitializer {
    private final EntityManagerFactory emf;

    public DataInitializer(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initialiser() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<Utilisateur> utilisateurs = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                Utilisateur user = new Utilisateur("Utilisateur " + i, "utilisateur" + i + "@example.com");
                em.persist(user);
                utilisateurs.add(user);
            }

            List<Salle> salles = new ArrayList<>();
            salles.add(new Salle("Salle Atlas", 12, "Salle de réunion avec écran"));
            salles.add(new Salle("Salle Rif", 24, "Espace de formation"));
            salles.add(new Salle("Salle Toubkal", 40, "Grande salle de conférence"));
            for (Salle salle : salles) {
                em.persist(salle);
            }

            LocalDateTime demain = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
            for (int i = 0; i < 9; i++) {
                Salle salle = salles.get(i % salles.size());
                LocalDateTime debut = demain.plusDays(i / 3).plusHours((i % 3) * 3);
                em.persist(new Reservation(utilisateurs.get(i % utilisateurs.size()),
                        salle, debut, debut.plusHours(1)));
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
