package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;

import javax.persistence.OptimisticLockException;

public class ReservationRetryHandler {
    private static final int MAX_RETRIES = 3;
    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    public Reservation modifierAvecRetry(Reservation reservation) {
        Reservation desiredChanges = reservation;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return service.modifierReservation(desiredChanges);
            } catch (OptimisticLockException e) {
                if (attempt == MAX_RETRIES) {
                    throw e;
                }
                Reservation latest = service.trouverParId(reservation.getId());
                if (latest == null) {
                    throw new IllegalArgumentException("Réservation non trouvée : " + reservation.getId());
                }
                latest.setSalle(reservation.getSalle());
                latest.setDateDebut(reservation.getDateDebut());
                latest.setDateFin(reservation.getDateFin());
                desiredChanges = latest;
            }
        }
        throw new IllegalStateException("La stratégie de retry a quitté sa boucle sans résultat.");
    }
}
