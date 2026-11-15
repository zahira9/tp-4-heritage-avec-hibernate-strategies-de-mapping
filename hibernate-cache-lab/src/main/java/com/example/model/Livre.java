package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "livres")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Livre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id")
    private Auteur auteur;

    protected Livre() {
    }

    public Livre(String titre) {
        this.titre = titre;
    }

    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Auteur getAuteur() {
        return auteur;
    }

    void setAuteur(Auteur auteur) {
        this.auteur = auteur;
    }
}
