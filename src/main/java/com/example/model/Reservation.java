package com.example.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @Column(nullable = false)
    private LocalDateTime debut;

    @Column(nullable = false)
    private LocalDateTime fin;

    public Reservation() {
    }

    public Reservation(LocalDateTime debut, LocalDateTime fin) {
        setDebut(debut);
        setFin(fin);
    }

    public Long getId() {
        return id;
    }

    public Salle getSalle() {
        return salle;
    }

    public void setSalle(Salle salle) {
        this.salle = salle;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = Objects.requireNonNull(debut, "La date de debut est obligatoire.");
        validerPeriodeSiComplete();
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = Objects.requireNonNull(fin, "La date de fin est obligatoire.");
        validerPeriodeSiComplete();
    }

    private void validerPeriodeSiComplete() {
        if (debut != null && fin != null && !debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de debut doit preceder la date de fin.");
        }
    }
}
