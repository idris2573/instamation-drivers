package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.repository.AccountRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class StartUpMethods {

    private static final Logger logger = Logger.getLogger(StartUpMethods.class);

    @Autowired
    private AccountRepository accountRepository;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        removeAutomationLocksAndLogoutAccounts();
    }

    private void removeAutomationLocksAndLogoutAccounts(){
        List<Account> accounts = accountRepository.findAll();
        for(Account account : accounts){
            account.setAutomationLock(false);
            account.setRunning(false);
            account.setLoggedIn(false);
        }
        accountRepository.saveAll(accounts);
        logger.info("Automation locks removed and accounts logged out.");
    }
}
