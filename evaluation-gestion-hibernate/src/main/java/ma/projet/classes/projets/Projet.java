package ma.projet.classes.projets;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "projets")
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chef_projet_id", nullable = false)
    private Employe chefProjet;

    @OneToMany(mappedBy = "projet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches = new ArrayList<>();

    protected Projet() {
    }

    public Projet(String nom, LocalDate dateDebut, LocalDate dateFin, Employe chefProjet) {
        setNom(nom);
        setDates(dateDebut, dateFin);
        setChefProjet(chefProjet);
    }

    public void addTache(Tache tache) {
        if (tache == null) {
            throw new IllegalArgumentException("La tâche est obligatoire.");
        }
        tache.setProjet(this);
    }

    void addTacheReference(Tache tache) {
        if (!taches.contains(tache)) {
            taches.add(tache);
        }
    }

    void removeTacheReference(Tache tache) {
        taches.remove(tache);
    }

    private void setDates(LocalDate debut, LocalDate fin) {
        if (debut == null || (fin != null && fin.isBefore(debut))) {
            throw new IllegalArgumentException("Les dates du projet sont invalides.");
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
            throw new IllegalArgumentException("Le nom du projet est obligatoire.");
        }
        this.nom = nom.trim();
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public Employe getChefProjet() {
        return chefProjet;
    }

    public void setChefProjet(Employe chefProjet) {
        if (chefProjet == null) {
            throw new IllegalArgumentException("Le chef de projet est obligatoire.");
        }
        this.chefProjet = chefProjet;
    }

    public List<Tache> getTaches() {
        return Collections.unmodifiableList(taches);
    }
}
