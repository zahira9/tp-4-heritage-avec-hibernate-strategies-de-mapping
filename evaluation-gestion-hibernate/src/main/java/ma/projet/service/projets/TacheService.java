package ma.projet.service.projets;

import ma.projet.classes.projets.Tache;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class TacheService extends GenericDao<Tache, Long> {
    public TacheService() {
        super(Tache.class);
    }

    public List<Tache> findPrixSuperieurA1000() {
        try (Session session = sessionFactory.openSession()) {
            return session.createNamedQuery("Tache.prixSuperieurA1000", Tache.class).getResultList();
        }
    }

    public List<Tache> findRealiseesBetween(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || debut.isAfter(fin)) {
            throw new IllegalArgumentException("La période de réalisation est invalide.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT et.tache FROM EmployeTache et "
                                    + "WHERE et.dateDebutReelle IS NOT NULL "
                                    + "AND et.dateDebutReelle >= :debut AND et.dateFinReelle <= :fin",
                            Tache.class)
                    .setParameter("debut", debut).setParameter("fin", fin).getResultList();
        }
    }
}
