package com.instamation.drivers.authenitcation;

import com.instamation.drivers.model.User;
import com.instamation.drivers.model.UserType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public interface UserService extends UserDetailsService {
    User findByUsername(String username);
    void createUser(User user);
    void updateUser(User user);
    User findById(Long id);
    List<User> findAll();
    List<User> findByUserType(UserType userType);
    List<User> findByUserTypeAndRegisterDateAfter(UserType userType, Date date);
    User findByEmail(String email);


}
