package com.example.service;

import com.example.model.Auteur;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {
    private final EntityManagerFactory emf;
    private final Statistics statistics;

    public PerformanceTestService(EntityManagerFactory emf) {
        this.emf = emf;
        this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public QueryMetrics mesurerNPlusUn() {
        return mesurerAssociations("SELECT a FROM Auteur a ORDER BY a.id", false);
    }

    public QueryMetrics mesurerJoinFetch() {
        return mesurerAssociations("SELECT DISTINCT a FROM Auteur a LEFT JOIN FETCH a.livres ORDER BY a.id", false);
    }

    public QueryMetrics mesurerEntityGraph() {
        return mesurerAssociations("SELECT a FROM Auteur a ORDER BY a.id", true);
    }

    private QueryMetrics mesurerAssociations(String jpql, boolean utiliserEntityGraph) {
        viderCaches();
        statistics.clear();
        long start = System.nanoTime();
        EntityManager em = emf.createEntityManager();
        int auteurCount;
        int livreCount = 0;
        try {
            TypedQuery<Auteur> query = em.createQuery(jpql, Auteur.class);
            if (utiliserEntityGraph) {
                EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
                query.setHint("javax.persistence.fetchgraph", graph);
            }
            List<Auteur> auteurs = query.getResultList();
            auteurCount = auteurs.size();
            for (Auteur auteur : auteurs) {
                livreCount += auteur.getLivres().size();
            }
        } finally {
            em.close();
        }
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        return new QueryMetrics(auteurCount, livreCount, statistics.getPrepareStatementCount(), elapsedMillis);
    }

    public CacheMetrics mesurerCacheSecondNiveau(Long auteurId) {
        viderCaches();
        statistics.clear();
        lireAuteurDansNouvelleSession(auteurId);
        long requetesApresPremierChargement = statistics.getPrepareStatementCount();
        lireAuteurDansNouvelleSession(auteurId);
        long requetesTotales = statistics.getPrepareStatementCount();
        return new CacheMetrics(
                requetesApresPremierChargement,
                requetesTotales - requetesApresPremierChargement,
                statistics.getSecondLevelCacheHitCount(),
                statistics.getSecondLevelCacheMissCount());
    }

    private void lireAuteurDansNouvelleSession(Long auteurId) {
        EntityManager em = emf.createEntityManager();
        try {
            Auteur auteur = em.find(Auteur.class, auteurId);
            if (auteur == null) {
                throw new IllegalArgumentException("Auteur non trouvé : " + auteurId);
            }
            auteur.getNom();
        } finally {
            em.close();
        }
    }

    private void viderCaches() {
        emf.getCache().evictAll();
        statistics.clear();
    }

    public static final class QueryMetrics {
        private final int auteurs;
        private final int livres;
        private final long requetesSql;
        private final long dureeMillis;

        private QueryMetrics(int auteurs, int livres, long requetesSql, long dureeMillis) {
            this.auteurs = auteurs;
            this.livres = livres;
            this.requetesSql = requetesSql;
            this.dureeMillis = dureeMillis;
        }

        public int getAuteurs() {
            return auteurs;
        }

        public int getLivres() {
            return livres;
        }

        public long getRequetesSql() {
            return requetesSql;
        }

        public long getDureeMillis() {
            return dureeMillis;
        }
    }

    public static final class CacheMetrics {
        private final long requetesPremierChargement;
        private final long requetesSecondChargement;
        private final long hits;
        private final long misses;

        private CacheMetrics(long requetesPremierChargement, long requetesSecondChargement,
                             long hits, long misses) {
            this.requetesPremierChargement = requetesPremierChargement;
            this.requetesSecondChargement = requetesSecondChargement;
            this.hits = hits;
            this.misses = misses;
        }

        public long getRequetesPremierChargement() {
            return requetesPremierChargement;
        }

        public long getRequetesSecondChargement() {
            return requetesSecondChargement;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }
    }
}
