package ma.projet.service.projets;

import ma.projet.classes.projets.Employe;
import ma.projet.classes.projets.Projet;
import ma.projet.classes.projets.Tache;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.util.List;

public class EmployeService extends GenericDao<Employe, Long> {
    public EmployeService() {
        super(Employe.class);
    }

    public List<Tache> findTachesRealisees(Long employeId) {
        if (employeId == null) {
            throw new IllegalArgumentException("L'employé est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT et.tache FROM EmployeTache et WHERE et.employe.id = :id "
                                    + "AND et.dateDebutReelle IS NOT NULL ORDER BY et.tache.id",
                            Tache.class)
                    .setParameter("id", employeId).getResultList();
        }
    }

    public List<Projet> findProjetsGeres(Long employeId) {
        if (employeId == null) {
            throw new IllegalArgumentException("L'employé est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT p FROM Projet p WHERE p.chefProjet.id = :id ORDER BY p.id",
                            Projet.class)
                    .setParameter("id", employeId).getResultList();
        }
    }
}
