package ma.projet.util;

import ma.projet.beans.Femme;
import ma.projet.beans.Homme;
import ma.projet.beans.Mariage;
import ma.projet.beans.Personne;
import ma.projet.classes.projets.Employe;
import ma.projet.classes.projets.EmployeTacheId;
import ma.projet.classes.projets.EmployeTache;
import ma.projet.classes.projets.Projet;
import ma.projet.classes.projets.Tache;
import ma.projet.classes.stock.Categorie;
import ma.projet.classes.stock.Commande;
import ma.projet.classes.stock.LigneCommande;
import ma.projet.classes.stock.LigneCommandeId;
import ma.projet.classes.stock.Produit;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil() {
    }

    private static SessionFactory buildSessionFactory() {
        Properties properties = new Properties();
        try (InputStream input = HibernateUtil.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties est introuvable.");
            }
            properties.load(input);
            overrideFromEnvironment(properties, "DB_DRIVER", "hibernate.connection.driver_class");
            overrideFromEnvironment(properties, "DB_URL", "hibernate.connection.url");
            overrideFromEnvironment(properties, "DB_USER", "hibernate.connection.username");
            overrideFromEnvironment(properties, "DB_PASSWORD", "hibernate.connection.password");
            overrideFromEnvironment(properties, "HIBERNATE_DIALECT", "hibernate.dialect");
            overrideFromEnvironment(properties, "HIBERNATE_DDL_AUTO", "hibernate.hbm2ddl.auto");
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }

        try {
            return new Configuration()
                    .addProperties(properties)
                    .addAnnotatedClass(Categorie.class)
                    .addAnnotatedClass(Produit.class)
                    .addAnnotatedClass(Commande.class)
                    .addAnnotatedClass(LigneCommande.class)
                    .addAnnotatedClass(Projet.class)
                    .addAnnotatedClass(Tache.class)
                    .addAnnotatedClass(Employe.class)
                    .addAnnotatedClass(EmployeTache.class)
                    .addAnnotatedClass(Homme.class)
                    .addAnnotatedClass(Femme.class)
                    .addAnnotatedClass(Mariage.class)
                    .addAnnotatedClass(Personne.class)
                    .addAnnotatedClass(LigneCommandeId.class)
                    .addAnnotatedClass(EmployeTacheId.class)
                    .buildSessionFactory();
        } catch (RuntimeException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void overrideFromEnvironment(Properties properties, String environmentKey,
                                                String propertyKey) {
        String value = System.getenv(environmentKey);
        if (value != null && !value.trim().isEmpty()) {
            properties.setProperty(propertyKey, value.trim());
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    public static void shutdown() {
        if (!SESSION_FACTORY.isClosed()) {
            SESSION_FACTORY.close();
        }
    }
}
