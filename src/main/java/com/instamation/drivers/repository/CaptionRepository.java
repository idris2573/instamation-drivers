package com.instamation.drivers.repository;

import com.instamation.drivers.model.Caption;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CaptionRepository extends PagingAndSortingRepository<Caption, Long> {
    List<Caption> findAll();

    @Query(value = "SELECT * FROM captions ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Caption findRandom();
}
