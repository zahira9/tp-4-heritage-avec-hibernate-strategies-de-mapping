# TP 6 : Simuler un conflit de réservation concurrent et démontrer l'optimistic locking avec @Version

Projet Maven autonome qui illustre le verrouillage optimiste JPA/Hibernate avec une base H2 en mémoire.

## Prérequis

- Java 8 ou supérieur
- Maven 3.6 ou supérieur

## Exécution

Depuis ce dossier :

```shell
mvn clean test
mvn exec:java
```

Le simulateur ouvre deux `EntityManager` et deux transactions, charge la même réservation dans chacune, puis valide la première modification. La deuxième transaction échoue avec `OptimisticLockException`, car sa version est périmée. Le gestionnaire de retry recharge ensuite l'entité et applique la modification demandée sur la version courante.

## Mécanisme

Le champ `@Version` de `Reservation` est initialisé par JPA et incrémenté à chaque mise à jour. Hibernate inclut la version lue dans la condition de mise à jour SQL ; si aucune ligne ne correspond, une écriture concurrente a eu lieu et le commit est refusé. Les transactions doivent rester courtes et l'application doit choisir entre rechargement/retry, résolution métier ou notification à l'utilisateur.

La stratégie de retry est bornée à trois tentatives. Chaque nouvel essai réutilise une instance rechargée, jamais l'entité obsolète après un échec.
