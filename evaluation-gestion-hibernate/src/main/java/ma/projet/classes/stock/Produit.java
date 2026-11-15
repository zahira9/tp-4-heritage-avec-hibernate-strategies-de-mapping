package ma.projet.classes.stock;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "stock_produits")
@NamedQuery(name = "StockProduit.priceGreaterThan",
        query = "SELECT p FROM Produit p WHERE p.prix > :prix ORDER BY p.reference")
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String reference;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "produit", cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    protected Produit() {
    }

    public Produit(String reference, BigDecimal prix, Categorie categorie) {
        setReference(reference);
        setPrix(prix);
        setCategorie(categorie);
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("La référence produit est obligatoire.");
        }
        this.reference = reference.trim();
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        if (prix == null || prix.signum() <= 0) {
            throw new IllegalArgumentException("Le prix doit être supérieur à zéro.");
        }
        this.prix = prix;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        if (categorie == null) {
            throw new IllegalArgumentException("Le produit doit être classé dans une catégorie.");
        }
        if (this.categorie == categorie) {
            return;
        }
        Categorie ancienne = this.categorie;
        this.categorie = categorie;
        if (ancienne != null) {
            ancienne.removeProduitReference(this);
        }
        categorie.addProduitReference(this);
    }

    public List<LigneCommande> getLignes() {
        return Collections.unmodifiableList(lignes);
    }
}
