package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "salles")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(name = "Salle.reservations", attributeNodes = @NamedAttributeNode("reservations"))
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    @Column(length = 255)
    private String description;

    @Version
    private int version;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Reservation> reservations = new ArrayList<>();

    protected Salle() {
    }

    public Salle(String nom, int capacite, String description) {
        setNom(nom);
        setCapacite(capacite);
        setDescription(description);
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la salle est obligatoire.");
        }
        this.nom = nom.trim();
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        if (capacite <= 0) {
            throw new IllegalArgumentException("La capacité doit être supérieure à zéro.");
        }
        this.capacite = capacite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public int getVersion() {
        return version;
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public void ajouterReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La réservation est obligatoire.");
        }
        reservations.add(reservation);
        reservation.setSalle(this);
    }
}
