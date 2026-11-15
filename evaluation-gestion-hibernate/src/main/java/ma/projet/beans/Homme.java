package ma.projet.beans;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "civil_hommes")
public class Homme extends Personne {
    protected Homme() {
    }

    public Homme(String nom, String prenom, String telephone, String adresse,
                 LocalDate dateNaissance) {
        super(nom, prenom, telephone, adresse, dateNaissance);
    }
}
