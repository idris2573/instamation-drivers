package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Follower;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FollowerRepository extends PagingAndSortingRepository<Follower, Long> {
    List<Follower> findAll();
    List<Follower> findByAccount(Account account);
    Follower findByAccountAndUsername(Account account, String username);
    List<Follower> findFirst20ByAccountOrderByIdDesc(Account account);
}
