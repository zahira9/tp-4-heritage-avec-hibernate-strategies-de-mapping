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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "stock_categories")
public class Categorie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String libelle;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Produit> produits = new ArrayList<>();

    protected Categorie() {
    }

    public Categorie(String code, String libelle) {
        setCode(code);
        setLibelle(libelle);
    }

    public void addProduit(Produit produit) {
        if (produit == null) {
            throw new IllegalArgumentException("Le produit est obligatoire.");
        }
        produit.setCategorie(this);
    }

    void addProduitReference(Produit produit) {
        if (!produits.contains(produit)) {
            produits.add(produit);
        }
    }

    void removeProduitReference(Produit produit) {
        produits.remove(produit);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Le code de catégorie est obligatoire.");
        }
        this.code = code.trim();
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé de catégorie est obligatoire.");
        }
        this.libelle = libelle.trim();
    }

    public List<Produit> getProduits() {
        return Collections.unmodifiableList(produits);
    }

    @Override
    public String toString() {
        return "Categorie{id=" + id + ", code='" + code + "', libelle='" + libelle + "'}";
    }
}
