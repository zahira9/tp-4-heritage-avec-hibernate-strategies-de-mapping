package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "produits")
@NamedQueries({
        @NamedQuery(name = "Produit.findAll", query = "SELECT p FROM Produit p ORDER BY p.id"),
        @NamedQuery(name = "Produit.findByCategorie",
                query = "SELECT p FROM Produit p WHERE p.categorie.id = :catId ORDER BY p.id"),
        @NamedQuery(name = "Produit.searchByLibelle",
                query = "SELECT p FROM Produit p WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :term, '%')) ORDER BY p.id")
})
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    protected Produit() {
    }

    public Produit(String libelle, BigDecimal prix) {
        setLibelle(libelle);
        setPrix(prix);
        this.dateCreation = LocalDate.now();
    }

    public Produit(String libelle, BigDecimal prix, Categorie categorie) {
        this(libelle, prix);
        if (categorie == null) {
            throw new IllegalArgumentException("Le produit doit être associé à une catégorie.");
        }
        setCategorie(categorie);
    }

    public Long getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        if (libelle == null || libelle.trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé du produit est obligatoire.");
        }
        this.libelle = libelle.trim();
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

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        if (dateCreation == null) {
            throw new IllegalArgumentException("La date de création est obligatoire.");
        }
        this.dateCreation = dateCreation;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        if (this.categorie == categorie) {
            return;
        }
        Categorie ancienneCategorie = this.categorie;
        this.categorie = categorie;
        if (ancienneCategorie != null) {
            ancienneCategorie.removeProduitReference(this);
        }
        if (categorie != null) {
            categorie.addProduitReference(this);
        }
    }

    @Override
    public String toString() {
        return "Produit{id=" + id + ", libelle='" + libelle + "', prix=" + prix + "}";
    }
}
