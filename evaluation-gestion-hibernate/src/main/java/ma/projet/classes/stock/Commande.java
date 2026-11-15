package ma.projet.classes.stock;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "stock_commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LigneCommande> lignes = new ArrayList<>();

    protected Commande() {
    }

    public Commande(LocalDate date) {
        setDate(date);
    }

    public LigneCommande addProduit(Produit produit, int quantite) {
        LigneCommande ligne = new LigneCommande(this, produit, quantite);
        lignes.add(ligne);
        return ligne;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La date de commande est obligatoire.");
        }
        this.date = date;
    }

    public List<LigneCommande> getLignes() {
        return Collections.unmodifiableList(lignes);
    }
}
