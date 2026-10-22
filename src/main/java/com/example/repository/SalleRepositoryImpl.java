package com.example.repository;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.util.PageResult;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SalleRepositoryImpl implements SalleRepository {

    private final EntityManager em;

    public SalleRepositoryImpl(EntityManager em) {
        this.em = Objects.requireNonNull(em, "em");
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin) {
        validerPeriode(debut, fin);
        return em.createQuery(
                        "SELECT s FROM Salle s WHERE NOT EXISTS ("
                                + "SELECT r.id FROM Reservation r "
                                + "WHERE r.salle = s AND r.debut < :fin AND r.fin > :debut"
                                + ") ORDER BY s.nom, s.id",
                        Salle.class)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getResultList();
    }

    @Override
    public PageResult<Salle> search(SalleSearchCriteria criteria, int page, int size) {
        Objects.requireNonNull(criteria, "criteria");
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("La page et la taille doivent etre strictement positives.");
        }
        int offset;
        try {
            offset = Math.multiplyExact(page - 1, size);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("La page demandee depasse la limite supportee.", e);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Salle> select = cb.createQuery(Salle.class);
        Root<Salle> root = select.from(Salle.class);
        select.select(root)
                .where(predicates(criteria, cb, select, root).toArray(new Predicate[0]))
                .orderBy(cb.asc(root.get("nom")), cb.asc(root.get("id")));

        List<Salle> items = em.createQuery(select)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
        return new PageResult<>(items, page, size, count(criteria));
    }

    @Override
    public long count(SalleSearchCriteria criteria) {
        Objects.requireNonNull(criteria, "criteria");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> select = cb.createQuery(Long.class);
        Root<Salle> root = select.from(Salle.class);
        select.select(cb.count(root))
                .where(predicates(criteria, cb, select, root).toArray(new Predicate[0]));
        return em.createQuery(select).getSingleResult();
    }

    private List<Predicate> predicates(SalleSearchCriteria criteria, CriteriaBuilder cb,
                                       CriteriaQuery<?> query, Root<Salle> salle) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria.getNom() != null) {
            predicates.add(cb.like(cb.lower(salle.<String>get("nom")),
                    "%" + criteria.getNom().toLowerCase(java.util.Locale.ROOT) + "%"));
        }
        if (criteria.getCapaciteMin() != null) {
            predicates.add(cb.ge(salle.<Integer>get("capacite"), criteria.getCapaciteMin()));
        }
        if (criteria.getDebut() != null) {
            Subquery<Long> overlaps = query.subquery(Long.class);
            Root<Reservation> reservation = overlaps.from(Reservation.class);
            overlaps.select(reservation.<Long>get("id"));
            overlaps.where(
                    cb.equal(reservation.get("salle"), salle),
                    cb.lessThan(reservation.<LocalDateTime>get("debut"), criteria.getFin()),
                    cb.greaterThan(reservation.<LocalDateTime>get("fin"), criteria.getDebut()));
            predicates.add(cb.not(cb.exists(overlaps)));
        }
        return predicates;
    }

    private void validerPeriode(LocalDateTime debut, LocalDateTime fin) {
        Objects.requireNonNull(debut, "La date de debut est obligatoire.");
        Objects.requireNonNull(fin, "La date de fin est obligatoire.");
        if (!debut.isBefore(fin)) {
            throw new IllegalArgumentException("La date de debut doit preceder la date de fin.");
        }
    }
}
