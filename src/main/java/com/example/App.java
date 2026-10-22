package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.repository.SalleRepositoryImpl;
import com.example.service.SalleService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public final class App {

    private App() {
    }

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("SallePU");
        try {
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();
                Salle atlas = new Salle("Salle Atlas", 12);
                Salle toubkal = new Salle("Salle Toubkal", 30);
                Salle oasis = new Salle("Salle Oasis", 8);
                em.persist(atlas);
                em.persist(toubkal);
                em.persist(oasis);
                atlas.addReservation(new Reservation(
                        LocalDateTime.of(2026, 11, 2, 9, 0),
                        LocalDateTime.of(2026, 11, 2, 11, 0)));
                em.getTransaction().commit();

                SalleService service = new SalleService(new SalleRepositoryImpl(em));
                LocalDateTime debut = LocalDateTime.of(2026, 11, 2, 10, 0);
                LocalDateTime fin = LocalDateTime.of(2026, 11, 2, 12, 0);
                System.out.println("Salles disponibles : "
                        + service.getAvailableRooms(debut, fin).size());
                service.searchRooms("salle", 10, debut, fin, 1, 5).getItems()
                        .forEach(salle -> System.out.println(salle.getNom()));
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } finally {
                em.close();
            }
        } finally {
            emf.close();
        }
    }
}
