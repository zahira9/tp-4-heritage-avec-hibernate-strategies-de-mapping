package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        try {
            new DataInitService(emf).initialiser(10, 5);
            PerformanceTestService service = new PerformanceTestService(emf);

            print("Sans optimisation (N+1)", service.mesurerNPlusUn());
            print("Avec JOIN FETCH", service.mesurerJoinFetch());
            print("Avec Entity Graph", service.mesurerEntityGraph());

            EntityManager em = emf.createEntityManager();
            Long auteurId;
            try {
                auteurId = em.createQuery("SELECT MIN(a.id) FROM Auteur a", Long.class).getSingleResult();
            } finally {
                em.close();
            }
            PerformanceTestService.CacheMetrics cache = service.mesurerCacheSecondNiveau(auteurId);
            System.out.println("Cache L2 - requêtes première lecture: " + cache.getRequetesPremierChargement()
                    + ", seconde lecture: " + cache.getRequetesSecondChargement()
                    + ", hits: " + cache.getHits() + ", misses: " + cache.getMisses());
        } finally {
            emf.close();
        }
    }

    private static void print(String label, PerformanceTestService.QueryMetrics metrics) {
        System.out.println(label + " - auteurs: " + metrics.getAuteurs()
                + ", livres: " + metrics.getLivres()
                + ", requêtes SQL: " + metrics.getRequetesSql()
                + ", durée: " + metrics.getDureeMillis() + " ms");
    }
}
