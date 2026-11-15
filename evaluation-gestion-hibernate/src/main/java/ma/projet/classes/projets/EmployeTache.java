package ma.projet.classes.projets;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "employe_tache")
public class EmployeTache {
    @EmbeddedId
    private EmployeTacheId id = new EmployeTacheId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("employeId")
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tacheId")
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @Column(name = "date_debut_reelle")
    private LocalDate dateDebutReelle;

    @Column(name = "date_fin_reelle")
    private LocalDate dateFinReelle;

    protected EmployeTache() {
    }

    public EmployeTache(Employe employe, Tache tache, LocalDate dateDebutReelle,
                        LocalDate dateFinReelle) {
        if (employe == null || tache == null) {
            throw new IllegalArgumentException("L'employé et la tâche sont obligatoires.");
        }
        this.employe = employe;
        this.tache = tache;
        setDatesReelles(dateDebutReelle, dateFinReelle);
    }

    public void setDatesReelles(LocalDate debut, LocalDate fin) {
        if ((debut == null) != (fin == null) || (debut != null && fin.isBefore(debut))) {
            throw new IllegalArgumentException("Les dates réelles sont incomplètes ou invalides.");
        }
        dateDebutReelle = debut;
        dateFinReelle = fin;
    }

    public EmployeTacheId getId() {
        return id;
    }

    public Employe getEmploye() {
        return employe;
    }

    public Tache getTache() {
        return tache;
    }

    public LocalDate getDateDebutReelle() {
        return dateDebutReelle;
    }

    public LocalDate getDateFinReelle() {
        return dateFinReelle;
    }

    public boolean isRealisee() {
        return dateDebutReelle != null;
    }
}
