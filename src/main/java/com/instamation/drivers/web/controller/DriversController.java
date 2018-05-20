package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Response;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/drivers")
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
public class DriversController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(value = "/all")
    public Set<Account> getAll(){
        Set<Account> accounts = new HashSet<>(DriverList.getDrivers().keySet());
        return accounts;
    }

    @GetMapping(value = "/running/{username}")
    public Boolean isAccountDriverRunning(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        return DriverList.containsKey(account);
    }

    @GetMapping(value = "/close/{username}")
    public Boolean closeDriverByUsername(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        DriverList.get(account).close();
         DriverList.remove(account);
        return true;
    }


    @GetMapping(value = "/loggedin/{username}")
    public Boolean isLoggedIn(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        Driver driver = DriverList.get(account);
        driver.getDriver().get("https://instagram.com");
        if(!Actions.doesButtonExist(driver, "Log In")) {
            return true;
        }else {
            return false;
        }
    }




}
