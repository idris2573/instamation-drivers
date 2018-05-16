package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {
    List<Comment> findAll();
    List<Comment> findByAccount(Account account);
    List<Comment> findByAccountAndDescription(Account account, String Description);

    @Query(value = "SELECT * FROM comments ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Comment findRandom();
}
