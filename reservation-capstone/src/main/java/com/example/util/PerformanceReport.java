package com.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManagerFactory;

public class PerformanceReport {
    private final Statistics statistics;

    public PerformanceReport(EntityManagerFactory emf) {
        this.statistics = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public String generer() {
        return "=== RAPPORT DE PERFORMANCE HIBERNATE ===\n"
                + "Requêtes exécutées : " + statistics.getQueryExecutionCount() + "\n"
                + "Statements SQL préparés : " + statistics.getPrepareStatementCount() + "\n"
                + "Cache L2 hits : " + statistics.getSecondLevelCacheHitCount() + "\n"
                + "Cache L2 misses : " + statistics.getSecondLevelCacheMissCount() + "\n"
                + "Cache L2 mises à jour : " + statistics.getSecondLevelCachePutCount() + "\n"
                + "Transactions : " + statistics.getTransactionCount() + "\n"
                + "Durée maximale d'une requête (ms) : " + statistics.getQueryExecutionMaxTime();
    }
}
