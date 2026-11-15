# TP 7 : Activation du cache de second niveau, optimisation des performances et suppression du problème N+1

Projet Maven autonome illustrant le cache de second niveau Hibernate/Ehcache 3, la mesure du problème N+1 et ses solutions avec `JOIN FETCH` et Entity Graphs.

## Prérequis et exécution

- Java 8 ou supérieur
- Maven 3.6 ou supérieur

Depuis ce dossier :

```shell
mvn clean verify
mvn exec:java
```

Le programme crée dix auteurs, avec cinq livres chacun, puis affiche le nombre de requêtes SQL et la durée observée pour le chargement lazy, `JOIN FETCH`, Entity Graph et deux chargements d'un auteur dans des EntityManager distincts.

## Résultats attendus

- Chargement lazy des collections : une requête pour les auteurs, puis une requête par auteur (11 au total pour le jeu d'essai).
- `JOIN FETCH` et Entity Graph : auteurs et livres chargés avec une requête.
- Cache L2 : première lecture depuis la base, seconde lecture depuis le cache partagé entre EntityManager.

Les durées varient selon la machine ; les tests vérifient le nombre de requêtes SQL et les statistiques de cache plutôt que des temps de performance fragiles.
