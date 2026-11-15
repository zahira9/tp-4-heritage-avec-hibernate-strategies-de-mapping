package ma.projet.classes.projets;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "projet_taches")
@NamedQuery(name = "Tache.prixSuperieurA1000",
        query = "SELECT t FROM Tache t WHERE t.prix > 1000 ORDER BY t.id")
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    protected Tache() {
    }

    public Tache(String nom, LocalDate dateDebut, LocalDate dateFin, BigDecimal prix) {
        setNom(nom);
        setDates(dateDebut, dateFin);
        setPrix(prix);
    }

    private void setDates(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null || fin.isBefore(debut)) {
            throw new IllegalArgumentException("Les dates de la tâche sont invalides.");
        }
        dateDebut = debut;
        dateFin = fin;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la tâche est obligatoire.");
        }
        this.nom = nom.trim();
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.signum() < 0) {
            throw new IllegalArgumentException("Le prix de la tâche ne peut pas être négatif.");
        }
        this.prix = prix;
    }

    public Projet getProjet() {
        return projet;
    }

    public void setProjet(Projet projet) {
        if (projet == null) {
            throw new IllegalArgumentException("La tâche doit appartenir à un projet.");
        }
        if (this.projet == projet) {
            return;
        }
        Projet ancien = this.projet;
        this.projet = projet;
        if (ancien != null) {
            ancien.removeTacheReference(this);
        }
        projet.addTacheReference(this);
    }
}
