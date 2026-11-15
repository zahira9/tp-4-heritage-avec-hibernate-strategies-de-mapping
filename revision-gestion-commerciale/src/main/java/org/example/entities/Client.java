package org.example.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "clients")
public class Client extends Utilisateur {
    @Column(name = "date_inscription")
    private LocalDate dateInscription;

    protected Client() {
    }

    public Client(String nom, LocalDate dateInscription) {
        super(nom);
        setDateInscription(dateInscription);
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        if (dateInscription == null) {
            throw new IllegalArgumentException("La date d'inscription est obligatoire.");
        }
        this.dateInscription = dateInscription;
    }
}
