package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Response;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.selenium.LogInMethods;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/drivers")
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
public class DriversController {

    private static final Logger logger = Logger.getLogger(AutomationController.class);

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(value = "/all")
    public Set<Account> getAll(){
        logger.info("REQUEST: get all accounts");
        Set<Account> accounts = new HashSet<>(DriverList.getDrivers().keySet());
        return accounts;
    }

    @GetMapping(value = "/running/{username}")
    public Boolean isAccountDriverRunning(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        boolean isRunning = DriverList.containsKey(account);
        logger.info("REQUEST: Check if account" + username + "'s driver is running. || RESPONSE: " + isRunning);
        return isRunning;
    }

    @GetMapping(value = "/close/{username}")
    public Boolean closeDriverByUsername(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            logger.info("REQUEST: Close" + username + "'s driver. || RESPONSE: " + false + " (account does not exist)");
            return false;
        }

        Driver driver = DriverList.get(account);
        if(driver == null){
            logger.info("REQUEST: Close" + username + "'s driver. || RESPONSE: " + false + " (account does not have a driver)");
            return false;
        }

        try {
            driver.close();
        }catch (Exception e){
            logger.info("REQUEST: Close" + username + "'s driver. || RESPONSE: " + false + " (could not close driver)");
        }
        DriverList.remove(account);

        logger.info("REQUEST: Close" + username + "'s driver. || RESPONSE: " + true);
        return true;
    }


    @GetMapping(value = "/loggedin/{username}")
    public Boolean isLoggedIn(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
            return false;
        }

        Driver driver = DriverList.get(account);
        if(driver == null || driver.isClosed()){
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
            return false;
        }

        if(LogInMethods.isLoggedIn(driver)) {
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + true);
            return true;
        }else {
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
            return false;
        }
    }




}
