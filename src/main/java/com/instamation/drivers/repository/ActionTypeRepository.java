package com.instamation.drivers.repository;

import com.instamation.drivers.model.ActionType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ActionTypeRepository extends PagingAndSortingRepository<ActionType, Long> {
    List<ActionType> findAll();
    ActionType findByType(String type);
}
