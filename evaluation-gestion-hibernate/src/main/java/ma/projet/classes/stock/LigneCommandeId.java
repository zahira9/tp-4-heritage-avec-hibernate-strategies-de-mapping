package ma.projet.classes.stock;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LigneCommandeId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "commande_id")
    private Long commandeId;

    @Column(name = "produit_id")
    private Long produitId;

    public LigneCommandeId() {
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public Long getProduitId() {
        return produitId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LigneCommandeId)) {
            return false;
        }
        LigneCommandeId that = (LigneCommandeId) other;
        return Objects.equals(commandeId, that.commandeId) && Objects.equals(produitId, that.produitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandeId, produitId);
    }
}
