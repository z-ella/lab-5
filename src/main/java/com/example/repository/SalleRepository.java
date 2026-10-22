package com.example.repository;

import com.example.model.Salle;
import com.example.util.PageResult;

import java.time.LocalDateTime;
import java.util.List;

public interface SalleRepository {

    List<Salle> findAvailableRooms(LocalDateTime debut, LocalDateTime fin);

    PageResult<Salle> search(SalleSearchCriteria criteria, int page, int size);

    long count(SalleSearchCriteria criteria);
}
