package com.example.repository;

import java.time.LocalDateTime;

public final class SalleSearchCriteria {

    private final String nom;
    private final Integer capaciteMin;
    private final LocalDateTime debut;
    private final LocalDateTime fin;

    public SalleSearchCriteria(String nom, Integer capaciteMin, LocalDateTime debut, LocalDateTime fin) {
        this.nom = nom == null || nom.trim().isEmpty() ? null : nom.trim();
        if (capaciteMin != null && capaciteMin < 0) {
            throw new IllegalArgumentException("La capacite minimale ne peut pas etre negative.");
        }
        if ((debut == null) != (fin == null)) {
            throw new IllegalArgumentException("Les deux bornes de disponibilite sont obligatoires.");
        }
        if (debut != null && !debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de debut doit preceder la date de fin.");
        }
        this.capaciteMin = capaciteMin;
        this.debut = debut;
        this.fin = fin;
    }

    public String getNom() {
        return nom;
    }

    public Integer getCapaciteMin() {
        return capaciteMin;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }
}
