# Evaluation — Hibernate & JPA

Trois mini-applications correspondant aux exercices de l'évaluation MLIAEdu :

1. **Gestion de stock** (`ma.projet.classes.stock`) : catégories, produits, commandes et lignes de commande, avec clé composée; recherches par catégorie, commande, période et Named Query de prix.
2. **Gestion de projets** (`ma.projet.classes.projets`) : projets gérés par un employé, tâches planifiées et affectations employé-tâche à clé composée avec dates réelles; recherche nommée des tâches au-dessus de 1000 DH et recherche par période de réalisation.
3. **Etat civil** (`ma.projet.beans`) : héritage `JOINED` de `Personne` en `Homme` et `Femme`, mariages, requête native nommée pour les enfants, Named Query des femmes mariées plusieurs fois et Criteria API pour compter les hommes ayant quatre épouses sur une période.

L'interface générique `ma.projet.dao.IDao` et son implémentation `GenericDao` centralisent les opérations CRUD transactionnelles. `ma.projet.util.HibernateUtil` lit `application.properties` et gère une `SessionFactory`.

H2 en mémoire est utilisé par défaut afin d'exécuter l'évaluation sans serveur externe. Le pilote MySQL Connector/J est également inclus. Pour utiliser MySQL, créez la base `evaluation_hibernate` et définissez `DB_DRIVER=com.mysql.cj.jdbc.Driver`, `DB_URL=jdbc:mysql://localhost:3306/evaluation_hibernate?serverTimezone=UTC`, `DB_USER`, `DB_PASSWORD` et `HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect` dans l'environnement avant de démarrer l'application. Le schéma est généré par Hibernate.

## Vérification et démonstration

```shell
mvn clean verify
mvn exec:java "-Dexec.mainClass=ma.projet.App"
```

Le programme de démonstration crée les produits et commandes de l'exercice 1, les projets/tâches de l'exercice 2, puis **10 femmes et 5 hommes** ainsi que des mariages d'exemple pour vérifier l'exercice 3.
