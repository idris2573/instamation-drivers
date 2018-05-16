package com.instamation.drivers.repository;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Setting;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SettingRepository extends PagingAndSortingRepository<Setting, Long> {
    Setting findByAccount(Account account);
}
