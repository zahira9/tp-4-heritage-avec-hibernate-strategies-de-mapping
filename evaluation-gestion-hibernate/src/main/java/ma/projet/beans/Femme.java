package ma.projet.beans;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "civil_femmes")
@NamedQuery(name = "Femme.marriedAtLeastTwice",
        query = "SELECT f FROM Femme f WHERE "
                + "(SELECT COUNT(m) FROM Mariage m WHERE m.femme = f) >= 2 ORDER BY f.nom, f.prenom")
@NamedNativeQuery(name = "Femme.countChildrenBetween",
        query = "SELECT COALESCE(SUM(m.nbr_enfant), 0) FROM civil_mariages m "
                + "WHERE m.femme_id = :femmeId AND m.date_debut >= :debut "
                + "AND m.date_debut <= :fin")
public class Femme extends Personne {
    @OneToMany(mappedBy = "femme", fetch = FetchType.LAZY)
    private List<Mariage> mariages = new ArrayList<>();

    protected Femme() {
    }

    public Femme(String nom, String prenom, String telephone, String adresse,
                 LocalDate dateNaissance) {
        super(nom, prenom, telephone, adresse, dateNaissance);
    }

    public List<Mariage> getMariages() {
        return Collections.unmodifiableList(mariages);
    }
}
