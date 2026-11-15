package org.example.entities;

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
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande", nullable = false)
    private LocalDate dateCommande;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CommandeProduit> lignes = new ArrayList<>();

    protected Commande() {
    }

    public Commande(LocalDate dateCommande) {
        setDateCommande(dateCommande);
    }

    public CommandeProduit addProduit(Produit produit, int quantite) {
        CommandeProduit ligne = new CommandeProduit(this, produit, quantite);
        lignes.add(ligne);
        return ligne;
    }

    public void removeLigne(CommandeProduit ligne) {
        if (lignes.remove(ligne)) {
            ligne.setCommande(null);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        if (dateCommande == null) {
            throw new IllegalArgumentException("La date de commande est obligatoire.");
        }
        this.dateCommande = dateCommande;
    }

    public List<CommandeProduit> getLignes() {
        return Collections.unmodifiableList(lignes);
    }
}
