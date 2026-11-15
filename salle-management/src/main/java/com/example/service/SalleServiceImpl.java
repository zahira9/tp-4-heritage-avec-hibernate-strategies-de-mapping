package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.util.PaginationResult;

import java.time.LocalDateTime;
import java.util.List;

public class SalleServiceImpl implements SalleService {
    private final SalleRepository salleRepository;

    public SalleServiceImpl(SalleRepository salleRepository) {
        if (salleRepository == null) {
            throw new IllegalArgumentException("Le repository est obligatoire.");
        }
        this.salleRepository = salleRepository;
    }

    @Override
    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return salleRepository.findAvailableRooms(debut, fin);
    }

    @Override
    public PaginationResult<Salle> searchRooms(String nom, Integer capaciteMin,
                                               LocalDateTime debut, LocalDateTime fin,
                                               int page, int size) {
        List<Salle> items = salleRepository.searchRooms(nom, capaciteMin, debut, fin, page, size);
        long totalItems = salleRepository.countRooms(nom, capaciteMin, debut, fin);
        return new PaginationResult<>(items, page, size, totalItems);
    }
}
