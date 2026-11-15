# TP (Capstone) : Finaliser l'application « Réservation de salles » avec JPA & Hibernate

Application Java autonome qui réunit les entités `Utilisateur`, `Salle` et `Reservation`, les réservations concurrentes, les recherches JPA, le cache Hibernate/Ehcache, les migrations JDBC et un rapport de performance.

## Prérequis et lancement

- JDK 11 ou supérieur
- Maven 3.6 ou supérieur

Depuis ce dossier :

```shell
mvn clean verify
mvn exec:java
```

Le projet utilise H2 en mémoire pour être exécutable sans serveur de base de données. Le démarrage applique la migration `db/migration/v2_0_add_description.sql`, crée un jeu de données déterministe, vérifie les créneaux libres, réserve une salle et affiche des réservations chargées avec `JOIN FETCH` et Entity Graph, puis imprime les statistiques Hibernate.

## Fonctionnalités

- Modèle relationnel avec contraintes, relations lazy et `@Version` pour les modifications concurrentes.
- Vérification des chevauchements de créneaux sous verrou pessimiste de la salle, afin de sérialiser les créations concurrentes pour la même salle.
- Cache L2 Ehcache avec régions `READ_WRITE` pour les entités et la collection des réservations.
- Recherche de disponibilités JPQL, recherche dynamique Criteria API et pagination.
- Chargement des réservations avec les associations salle/utilisateur via JPQL `JOIN FETCH` ou Entity Graph.
- Migration SQL versionnée, exécutée via JDBC et réexécutable grâce aux clauses H2 `IF NOT EXISTS`.
- Tests fonctionnels pour les réservations, conflits de créneaux, verrouillage optimiste, recherches, cache et migration.
- Logs JDK via `java.util.logging`, transactions `RESOURCE_LOCAL` et rapport de statistiques Hibernate.

Les durées d'exécution ne sont pas utilisées comme assertions : les tests privilégient les résultats fonctionnels, les statistiques SQL/cache et la détection des conflits.
