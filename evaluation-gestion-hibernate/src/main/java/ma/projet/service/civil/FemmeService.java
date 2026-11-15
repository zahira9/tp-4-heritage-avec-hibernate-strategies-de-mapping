package ma.projet.service.civil;

import ma.projet.beans.Femme;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class FemmeService extends GenericDao<Femme, Long> {
    public FemmeService() {
        super(Femme.class);
    }

    public Femme findPlusAgee() {
        try (Session session = sessionFactory.openSession()) {
            List<Femme> femmes = session.createQuery(
                            "FROM Femme f ORDER BY f.dateNaissance ASC, f.id ASC", Femme.class)
                    .setMaxResults(1).getResultList();
            return femmes.isEmpty() ? null : femmes.get(0);
        }
    }

    public long countEnfantsEntre(Long femmeId, LocalDate debut, LocalDate fin) {
        if (femmeId == null || debut == null || fin == null || debut.isAfter(fin)) {
            throw new IllegalArgumentException("La femme ou la période de recherche est invalide.");
        }
        try (Session session = sessionFactory.openSession()) {
            Number count = (Number) session.createNamedQuery("Femme.countChildrenBetween")
                    .setParameter("femmeId", femmeId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getSingleResult();
            return count.longValue();
        }
    }

    public List<Femme> findMarieesAuMoinsDeuxFois() {
        try (Session session = sessionFactory.openSession()) {
            return session.createNamedQuery("Femme.marriedAtLeastTwice", Femme.class)
                    .getResultList();
        }
    }
}
