package ma.projet.service.civil;

import ma.projet.beans.Femme;
import ma.projet.beans.Homme;
import ma.projet.beans.Mariage;
import ma.projet.dao.GenericDao;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class HommeService extends GenericDao<Homme, Long> {
    public HommeService() {
        super(Homme.class);
    }

    public List<Femme> findEpousesEntre(Long hommeId, LocalDate debut, LocalDate fin) {
        validatePeriod(hommeId, debut, fin);
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT m.femme FROM Mariage m WHERE m.homme.id = :id "
                                    + "AND m.dateDebut <= :fin "
                                    + "AND (m.dateFin IS NULL OR m.dateFin >= :debut)",
                            Femme.class)
                    .setParameter("id", hommeId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getResultList();
        }
    }

    public List<Mariage> findMariagesDetails(Long hommeId) {
        if (hommeId == null) {
            throw new IllegalArgumentException("L'homme est obligatoire.");
        }
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT m FROM Mariage m JOIN FETCH m.femme "
                                    + "WHERE m.homme.id = :id ORDER BY m.dateDebut, m.id",
                            Mariage.class)
                    .setParameter("id", hommeId).getResultList();
        }
    }

    public long countHommesAvecQuatreEpousesEntre(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || debut.isAfter(fin)) {
            throw new IllegalArgumentException("La période de recherche est invalide.");
        }
        try (Session session = sessionFactory.openSession()) {
            javax.persistence.criteria.CriteriaBuilder builder = session.getCriteriaBuilder();
            javax.persistence.criteria.CriteriaQuery<Long> criteria =
                    builder.createQuery(Long.class);
            javax.persistence.criteria.Root<Homme> homme = criteria.from(Homme.class);
            javax.persistence.criteria.Subquery<Long> qualifies = criteria.subquery(Long.class);
            javax.persistence.criteria.Root<Mariage> mariage = qualifies.from(Mariage.class);

            qualifies.select(mariage.get("homme").<Long>get("id"))
                    .where(builder.between(mariage.<LocalDate>get("dateDebut"), debut, fin))
                    .groupBy(mariage.get("homme").get("id"))
                    .having(builder.ge(builder.countDistinct(mariage.get("femme").get("id")), 4L));
            criteria.select(builder.countDistinct(homme.get("id")))
                    .where(homme.get("id").in(qualifies));
            return session.createQuery(criteria).getSingleResult();
        }
    }

    private void validatePeriod(Long hommeId, LocalDate debut, LocalDate fin) {
        if (hommeId == null || debut == null || fin == null || debut.isAfter(fin)) {
            throw new IllegalArgumentException("L'homme ou la période de recherche est invalide.");
        }
    }
}
