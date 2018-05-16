package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Stats;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Date;
import java.util.List;

public interface StatsRepository extends PagingAndSortingRepository<Stats, Long> {
    List<Stats> findAll();
    List<Stats> findByAccount(Account account);
    List<Stats> findByAccountAndDateGreaterThanEqual(Account account, Date date);
}
