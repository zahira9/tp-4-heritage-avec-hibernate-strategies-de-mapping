# TP 5 : Requêtes avancées avec JPA - Salles disponibles, recherche multi-critères et pagination

Projet Maven Java 8+ utilisant JPA 2.2, Hibernate 5 et H2 pour gérer des salles et leurs réservations.

## Fonctionnalités

- Recherche JPQL des salles disponibles sur un créneau. Deux réservations sont considérées consécutives, et non conflictuelles, lorsque l'une se termine exactement au début de l'autre.
- Recherche dynamique via Criteria API : nom partiel, capacité minimale et disponibilité facultative.
- Pagination serveur avec nombre total de résultats et de pages.
- Couches `SalleRepository` et `SalleService`, avec validations des plages horaires et de la pagination.

## Exécution

Depuis ce dossier :

```shell
mvn clean test
mvn exec:java "-Dexec.mainClass=com.example.App"
```

Le programme crée des données d'exemple dans une base H2 en mémoire, affiche les salles disponibles et exécute une recherche multi-critères paginée.
