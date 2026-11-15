package ma.projet.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "civil_mariages")
public class Mariage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "homme_id", nullable = false)
    private Homme homme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "femme_id", nullable = false)
    private Femme femme;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "nbr_enfant", nullable = false)
    private int nbrEnfant;

    protected Mariage() {
    }

    public Mariage(Homme homme, Femme femme, LocalDate dateDebut, LocalDate dateFin,
                   int nbrEnfant) {
        if (homme == null || femme == null) {
            throw new IllegalArgumentException("Les époux sont obligatoires.");
        }
        this.homme = homme;
        this.femme = femme;
        setDates(dateDebut, dateFin);
        setNbrEnfant(nbrEnfant);
    }

    private void setDates(LocalDate debut, LocalDate fin) {
        if (debut == null || (fin != null && fin.isBefore(debut))) {
            throw new IllegalArgumentException("Les dates du mariage sont invalides.");
        }
        dateDebut = debut;
        dateFin = fin;
    }

    public Long getId() {
        return id;
    }

    public Homme getHomme() {
        return homme;
    }

    public Femme getFemme() {
        return femme;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public int getNbrEnfant() {
        return nbrEnfant;
    }

    public void setNbrEnfant(int nbrEnfant) {
        if (nbrEnfant < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut pas être négatif.");
        }
        this.nbrEnfant = nbrEnfant;
    }
}
