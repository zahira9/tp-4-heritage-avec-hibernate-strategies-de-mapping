package ma.projet.classes.stock;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "stock_lignes_commande")
public class LigneCommande {
    @EmbeddedId
    private LigneCommandeId id = new LigneCommandeId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    protected LigneCommande() {
    }

    public LigneCommande(Commande commande, Produit produit, int quantite) {
        if (commande == null || produit == null) {
            throw new IllegalArgumentException("La commande et le produit sont obligatoires.");
        }
        this.commande = commande;
        this.produit = produit;
        setQuantite(quantite);
    }

    public LigneCommandeId getId() {
        return id;
    }

    public Commande getCommande() {
        return commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }
        this.quantite = quantite;
    }
}
