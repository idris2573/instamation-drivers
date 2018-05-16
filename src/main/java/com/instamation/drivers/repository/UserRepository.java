package com.instamation.drivers.repository;

import com.instamation.drivers.model.User;
import com.instamation.drivers.model.UserType;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Date;
import java.util.List;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    List<User> findAll();
    User findByUsername(String username);
    User findByEmail(String email);
    List<User> findByUserType(UserType userType);
    List<User> findByUserTypeAndRegisterDateAfter(UserType userType, Date date);
}
