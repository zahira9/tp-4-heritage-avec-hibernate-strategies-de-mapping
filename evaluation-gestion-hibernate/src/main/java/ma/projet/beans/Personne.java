package ma.projet.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "civil_personnes")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Personne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 30)
    private String telephone;

    @Column(length = 250)
    private String adresse;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    protected Personne() {
    }

    protected Personne(String nom, String prenom, String telephone, String adresse,
                       LocalDate dateNaissance) {
        setNom(nom);
        setPrenom(prenom);
        this.telephone = telephone;
        this.adresse = adresse;
        setDateNaissance(dateNaissance);
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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        if (dateNaissance == null || dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance est invalide.");
        }
        this.dateNaissance = dateNaissance;
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " est obligatoire.");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", nom='" + nom + "', prenom='" + prenom + "'}";
    }
}
