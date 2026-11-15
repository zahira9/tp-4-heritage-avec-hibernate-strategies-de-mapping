# Révision — Gestion commerciale avec Hibernate & JPA

Projet Maven de révision qui met en pratique Hibernate 5, JPA 2.2 et les relations d'un mini-domaine de gestion commerciale.

## Contenu

- Configuration Hibernate XML et `HibernateUtil` (`SessionFactory` partagée, sessions fermées par unité de travail).
- `GenericDAO<T, ID>` et `CrudService<T, ID>` avec transactions CRUD.
- Entités `Categorie` et `Produit` en relation bidirectionnelle `OneToMany` / `ManyToOne`.
- Named Queries, recherche dynamique Criteria API et pagination JPQL.
- Héritage `JOINED` avec `Utilisateur`, `Client` et `Fournisseur`.
- Commandes avec entité associative `CommandeProduit` et clé composite `@EmbeddedId` / `@MapsId`.
- Rapport mensuel par catégorie : somme de `prix × quantité` via Criteria API, exposé par un servlet.

H2 en mémoire est configuré pour que le projet soit exécutable sans serveur de base de données. Pour un déploiement, adaptez les propriétés de connexion dans `src/main/resources/hibernate.cfg.xml` au serveur MySQL et remplacez le pilote H2 par MySQL Connector/J.

## Exécution et vérification

```shell
mvn clean verify
mvn exec:java "-Dexec.mainClass=org.example.App"
```

Le rapport du mois précédent est également disponible via le servlet `GET /rapports/ventes-mensuelles` une fois le WAR déployé sur un conteneur Servlet compatible `javax.servlet` (par exemple Tomcat 9).
