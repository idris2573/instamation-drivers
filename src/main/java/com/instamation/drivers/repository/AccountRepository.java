package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {
    List<Account> findAll();
    List<Account> findByRunningAndEnabled(boolean running, boolean enabled);
    List<Account> findByEnabled(boolean enabled);
    List<Account> findByUser(User user);
    Account findByUsername(String username);
}
