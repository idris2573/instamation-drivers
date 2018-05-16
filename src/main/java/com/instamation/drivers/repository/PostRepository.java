package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Post;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.sql.Time;
import java.util.List;

public interface PostRepository extends PagingAndSortingRepository<Post, Long> {
    List<Post> findAll();
    List<Post> findByAccount(Account account);
    List<Post> findByAccountAndPostDateAfter(Account account, Time time);
    List<Post> findByAccountAndPosted(Account account, boolean posted);
    Post findFirstByAccountAndPosted(Account account, boolean posted);
}
