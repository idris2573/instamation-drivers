package com.instamation.drivers.repository;

import com.instamation.drivers.model.UserType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserTypeRepository extends PagingAndSortingRepository<UserType, Long> {
    List<UserType> findAll();
    UserType findByName(String name);
    UserType findByRole(String role);
}
