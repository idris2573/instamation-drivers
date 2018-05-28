package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.DriverInfo;
import com.instamation.drivers.model.Response;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.selenium.LogInMethods;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping(value = "/drivers")
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
public class DriversController {

    private static final Logger logger = Logger.getLogger(AutomationController.class);

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping(value = "/all")
    public Map<Account, DriverInfo> getAll(){
        Map<Account, DriverInfo> drivers = new HashMap<>();

        for(Map.Entry driver : DriverList.getDrivers().entrySet()){
            Account accountEntry = (Account) driver.getKey();
            Driver driverEntry = (Driver) driver.getValue();

            if(driverEntry == null || accountEntry == null || driverEntry.isClosed() || accountEntry.getUsername() == null){
                continue;
            }

            DriverInfo driverInfo = new DriverInfo();
            try {
                driverInfo.setUrl(driverEntry.getDriver().getCurrentUrl());
            }catch (Exception e){}
            try {
                driverInfo.setTitle(driverEntry.getDriver().getTitle());
            }catch (Exception e){}
            try {
                driverInfo.setH1(driverEntry.getDriver().findElement(By.tagName("h1")).getText());
            }catch (Exception e){}
            try {
                driverInfo.setH2(driverEntry.getDriver().findElement(By.tagName("h2")).getText());
            }catch (Exception e){}
            try {
                driverInfo.setBody(driverEntry.getDriver().findElement(By.tagName("body")).getText());
            }catch (Exception e){}
            try {
                driverInfo.setHtml(driverEntry.getDriver().findElement(By.tagName("body")).getAttribute("innerHTML"));
            }catch (Exception e){}

            drivers.put(accountEntry, driverInfo);
        }

        logger.info("REQUEST: get all accounts");
        return drivers;
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

    @GetMapping(value = "/get/{username}")
    public Boolean get(@PathVariable String username, HttpServletRequest request){
        String url = request.getParameter("url");
        Account account = accountRepository.findByUsername(username);

        if(DriverList.containsKey(account)){
            Driver driver = DriverList.get(account);
            if(driver == null || driver.isClosed()){
                return false;
            }

            driver.getDriver().get(url);
            return true;
        }

        return false;
    }



}
