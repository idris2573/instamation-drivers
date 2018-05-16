package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Action;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ActionRepository extends PagingAndSortingRepository<Action, Long> {
    List<Action> findAll();
    List<Action> findByAccount(Account account);
    List<Action> findFirst20ByAccountOrderByDateDesc(Account account);
}
