package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Comment;
import com.instamation.drivers.model.Proxy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProxyRepository extends PagingAndSortingRepository<Proxy, Long> {
    List<Proxy> findAll();

    @Query(value = "SELECT * FROM proxies ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Proxy findRandom();
}
