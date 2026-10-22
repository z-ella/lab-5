package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.util.PageResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SalleRepositoryTest {

    private static EntityManagerFactory emf;

    @BeforeClass
    public static void openFactory() {
        emf = Persistence.createEntityManagerFactory("SallePU");
    }

    @AfterClass
    public static void closeFactory() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    @Before
    public void cleanDatabase() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Reservation").executeUpdate();
            em.createQuery("DELETE FROM Salle").executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void findsOnlyRoomsWithoutOverlappingReservations() {
        seedRooms();
        EntityManager em = emf.createEntityManager();
        try {
            SalleRepositoryImpl repository = new SalleRepositoryImpl(em);
            List<String> available = repository.findAvailableRooms(
                            LocalDateTime.of(2026, 11, 2, 10, 0),
                            LocalDateTime.of(2026, 11, 2, 12, 0))
                    .stream().map(Salle::getNom).collect(Collectors.toList());
            assertEquals(2, available.size());
            assertFalse(available.contains("Salle Atlas"));

            List<Salle> consecutive = repository.findAvailableRooms(
                    LocalDateTime.of(2026, 11, 2, 11, 0),
                    LocalDateTime.of(2026, 11, 2, 12, 0));
            assertTrue(consecutive.stream().anyMatch(s -> "Salle Atlas".equals(s.getNom())));
        } finally {
            em.close();
        }
    }

    @Test
    public void combinesOptionalNameCapacityAndAvailabilityCriteria() {
        seedRooms();
        EntityManager em = emf.createEntityManager();
        try {
            SalleRepository repository = new SalleRepositoryImpl(em);
            SalleSearchCriteria criteria = new SalleSearchCriteria(
                    "TOUB", 20,
                    LocalDateTime.of(2026, 11, 2, 10, 0),
                    LocalDateTime.of(2026, 11, 2, 12, 0));
            PageResult<Salle> result = repository.search(criteria, 1, 10);
            assertEquals(1, result.getTotalItems());
            assertEquals("Salle Toubkal", result.getItems().get(0).getNom());
            assertEquals(1, repository.count(criteria));
        } finally {
            em.close();
        }
    }

    @Test
    public void paginatesInStableOrderAndReportsTotals() {
        seedRooms();
        EntityManager em = emf.createEntityManager();
        try {
            SalleRepository repository = new SalleRepositoryImpl(em);
            SalleSearchCriteria allRooms = new SalleSearchCriteria(null, null, null, null);

            PageResult<Salle> first = repository.search(allRooms, 1, 2);
            PageResult<Salle> second = repository.search(allRooms, 2, 2);
            PageResult<Salle> empty = repository.search(allRooms, 3, 2);

            assertEquals(3, first.getTotalItems());
            assertEquals(2, first.getTotalPages());
            assertEquals(2, first.getItems().size());
            assertEquals("Salle Atlas", first.getItems().get(0).getNom());
            assertEquals("Salle Toubkal", second.getItems().get(0).getNom());
            assertTrue(empty.getItems().isEmpty());
            assertEquals(0, new PageResult<>(java.util.Collections.emptyList(), 1, 10, 0).getTotalPages());
        } finally {
            em.close();
        }
    }

    @Test
    public void rejectsInvalidPageAndIncompleteOrReversedRanges() {
        EntityManager em = emf.createEntityManager();
        try {
            SalleRepository repository = new SalleRepositoryImpl(em);
            try {
                repository.search(new SalleSearchCriteria(null, null, null, null), 0, 10);
                fail("La page doit etre strictement positive.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("page"));
            }
            try {
                new SalleSearchCriteria(null, null, LocalDateTime.now(), null);
                fail("Les deux bornes temporelles sont requises ensemble.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("bornes"));
            }
            try {
                new SalleSearchCriteria(null, -1, null, null);
                fail("La capacite minimale negative doit etre refusee.");
            } catch (IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains("capacite"));
            }
        } finally {
            em.close();
        }
    }

    private void seedRooms() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Salle atlas = new Salle("Salle Atlas", 12);
            Salle toubkal = new Salle("Salle Toubkal", 30);
            Salle oasis = new Salle("Salle Oasis", 8);
            atlas.addReservation(new Reservation(
                    LocalDateTime.of(2026, 11, 2, 9, 0),
                    LocalDateTime.of(2026, 11, 2, 11, 0)));
            em.persist(atlas);
            em.persist(toubkal);
            em.persist(oasis);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
