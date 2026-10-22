package com.example.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "salles")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();

    public Salle() {
    }

    public Salle(String nom, int capacite) {
        setNom(nom);
        setCapacite(capacite);
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
        if (capacite < 0) {
            throw new IllegalArgumentException("La capacite ne peut pas etre negative.");
        }
        this.capacite = capacite;
    }

    public Set<Reservation> getReservations() {
        return Collections.unmodifiableSet(reservations);
    }

    public void addReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reservation est obligatoire.");
        }
        if (reservations.add(reservation)) {
            reservation.setSalle(this);
        }
    }

    public void removeReservation(Reservation reservation) {
        if (reservations.remove(reservation)) {
            reservation.setSalle(null);
        }
    }
}
