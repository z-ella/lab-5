package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;
import com.example.repository.SalleSearchCriteria;
import com.example.util.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class SalleService {

    private final SalleRepository repository;

    public SalleService(SalleRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<Salle> getAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        return repository.findAvailableRooms(debut, fin);
    }

    public PageResult<Salle> searchRooms(String nom, Integer capaciteMin, LocalDateTime debut,
                                         LocalDateTime fin, int page, int size) {
        return repository.search(new SalleSearchCriteria(nom, capaciteMin, debut, fin), page, size);
    }

    public long countRooms(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        return repository.count(new SalleSearchCriteria(nom, capaciteMin, debut, fin));
    }
}
