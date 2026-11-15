package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "fournisseurs")
public class Fournisseur extends Utilisateur {
    @Column(length = 250)
    private String adresse;

    protected Fournisseur() {
    }

    public Fournisseur(String nom, String adresse) {
        super(nom);
        setAdresse(adresse);
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        if (adresse == null || adresse.trim().isEmpty()) {
            throw new IllegalArgumentException("L'adresse du fournisseur est obligatoire.");
        }
        this.adresse = adresse.trim();
    }
}
