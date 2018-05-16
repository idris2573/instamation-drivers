package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.ProfileSeed;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProfileSeedRepository extends PagingAndSortingRepository<ProfileSeed, Long> {
    List<ProfileSeed> findAll();
    List<ProfileSeed> findByAccount(Account account);
    List<ProfileSeed> findByAccountAndUsed(Account account, boolean used);
    List<ProfileSeed> findByAccountAndType(Account account, String type);
    List<ProfileSeed> findByAccountAndTypeAndName(Account account, String type, String name);

}
