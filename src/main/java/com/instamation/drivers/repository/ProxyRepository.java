package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Proxy;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProxyRepository extends PagingAndSortingRepository<Proxy, Long> {
    List<Proxy> findAll();
    Proxy findFirstByAccount(Account account);
}
