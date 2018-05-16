package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Profile;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProfileRepository extends PagingAndSortingRepository<Profile, Long> {
    List<Profile> findAll();
    List<Profile> findByAccount(Account account);
    List<Profile> findByAccountAndUsername(Account account, String username);
    List<Profile> findByAccountAndFollowing(Account account, boolean following);
    List<Profile> findByAccountAndFollowingAndUnfollowed(Account account, boolean following, boolean unfollowed);
    List<Profile> findByAccountAndUnfollowed(Account account, boolean unfollowed);
    List<Profile> findByAccountAndLiked(Account account, boolean liked);
    List<Profile> findByAccountAndCommented(Account account, boolean commented);


}
