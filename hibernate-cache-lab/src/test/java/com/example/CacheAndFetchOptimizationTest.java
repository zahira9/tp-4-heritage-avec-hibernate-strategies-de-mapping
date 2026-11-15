package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;
import org.hibernate.SessionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheAndFetchOptimizationTest {
    private static EntityManagerFactory emf;
    private PerformanceTestService performance;

    @BeforeClass
    public static void createFactory() {
        emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
    }

    @AfterClass
    public static void closeFactory() {
        emf.close();
    }

    @Before
    public void initializeData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Livre").executeUpdate();
            em.createQuery("DELETE FROM Auteur").executeUpdate();
            em.getTransaction().commit();
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        emf.getCache().evictAll();
        new DataInitService(emf).initialiser(10, 5);
        performance = new PerformanceTestService(emf);
    }

    @Test
    public void lazyCollectionsDemonstrateNPlusOne() {
        PerformanceTestService.QueryMetrics metrics = performance.mesurerNPlusUn();

        assertEquals(10, metrics.getAuteurs());
        assertEquals(50, metrics.getLivres());
        assertEquals(11, metrics.getRequetesSql());
    }

    @Test
    public void joinFetchLoadsAuthorsAndBooksInOneQuery() {
        PerformanceTestService.QueryMetrics metrics = performance.mesurerJoinFetch();

        assertEquals(10, metrics.getAuteurs());
        assertEquals(50, metrics.getLivres());
        assertEquals(1, metrics.getRequetesSql());
    }

    @Test
    public void entityGraphLoadsAuthorsAndBooksInOneQuery() {
        PerformanceTestService.QueryMetrics metrics = performance.mesurerEntityGraph();

        assertEquals(10, metrics.getAuteurs());
        assertEquals(50, metrics.getLivres());
        assertEquals(1, metrics.getRequetesSql());
    }

    @Test
    public void secondLevelCacheServesEntityAcrossEntityManagers() {
        EntityManager em = emf.createEntityManager();
        Long auteurId;
        try {
            auteurId = em.createQuery("SELECT MIN(a.id) FROM Auteur a", Long.class).getSingleResult();
        } finally {
            em.close();
        }

        PerformanceTestService.CacheMetrics metrics = performance.mesurerCacheSecondNiveau(auteurId);

        assertEquals(1, metrics.getRequetesPremierChargement());
        assertEquals(0, metrics.getRequetesSecondChargement());
        assertTrue(metrics.getHits() >= 1);
        assertTrue(emf.unwrap(SessionFactory.class).getStatistics()
                .getSecondLevelCacheHitCount() >= 1);
    }
}
