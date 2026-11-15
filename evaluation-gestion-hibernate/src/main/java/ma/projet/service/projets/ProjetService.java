package ma.projet.service.projets;

import ma.projet.classes.projets.Projet;
import ma.projet.classes.projets.Tache;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.util.List;

public class ProjetService extends GenericDao<Projet, Long> {
    public ProjetService() {
        super(Projet.class);
    }

    public List<Tache> findTachesPlanifiees(Long projetId) {
        if (projetId == null) {
            throw new IllegalArgumentException("Le projet est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT t FROM Tache t WHERE t.projet.id = :id ORDER BY t.dateDebut, t.id",
                            Tache.class)
                    .setParameter("id", projetId).getResultList();
        }
    }

    public List<TacheRealisee> findTachesRealisees(Long projetId) {
        if (projetId == null) {
            throw new IllegalArgumentException("Le projet est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            List<Object[]> rows = session.createQuery(
                            "SELECT DISTINCT t, et.dateDebutReelle, et.dateFinReelle "
                                    + "FROM Tache t JOIN EmployeTache et ON et.tache = t "
                                    + "WHERE t.projet.id = :id AND et.dateDebutReelle IS NOT NULL "
                                    + "ORDER BY et.dateDebutReelle, t.id", Object[].class)
                    .setParameter("id", projetId).getResultList();
            return rows.stream().map(row -> new TacheRealisee(
                    (Tache) row[0],
                    (java.time.LocalDate) row[1],
                    (java.time.LocalDate) row[2])).collect(java.util.stream.Collectors.toList());
        }
    }

    public static final class TacheRealisee {
        private final Tache tache;
        private final java.time.LocalDate dateDebutReelle;
        private final java.time.LocalDate dateFinReelle;

        public TacheRealisee(Tache tache, java.time.LocalDate debut, java.time.LocalDate fin) {
            this.tache = tache;
            this.dateDebutReelle = debut;
            this.dateFinReelle = fin;
        }

        public Tache getTache() {
            return tache;
        }

        public java.time.LocalDate getDateDebutReelle() {
            return dateDebutReelle;
        }

        public java.time.LocalDate getDateFinReelle() {
            return dateFinReelle;
        }
    }
}
