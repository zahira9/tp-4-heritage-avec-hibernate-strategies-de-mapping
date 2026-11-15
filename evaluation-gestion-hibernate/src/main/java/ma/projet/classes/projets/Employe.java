package ma.projet.classes.projets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "projet_employes")
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 30)
    private String telephone;

    @OneToMany(mappedBy = "chefProjet")
    private List<Projet> projetsGeres = new ArrayList<>();

    @OneToMany(mappedBy = "employe")
    private List<EmployeTache> affectations = new ArrayList<>();

    protected Employe() {
    }

    public Employe(String nom, String prenom, String telephone) {
        setNom(nom);
        setPrenom(prenom);
        this.telephone = telephone;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = required(nom, "Le nom");
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = required(prenom, "Le prénom");
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public List<Projet> getProjetsGeres() {
        return Collections.unmodifiableList(projetsGeres);
    }

    public List<EmployeTache> getAffectations() {
        return Collections.unmodifiableList(affectations);
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " est obligatoire.");
        }
        return value.trim();
    }
}
