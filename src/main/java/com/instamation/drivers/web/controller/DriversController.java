package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.DriverInfo;
import com.instamation.drivers.repository.AccountRepository;
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

    @Autowired
    private DriverList driverList;

    @GetMapping(value = "/all")
    public Map<Account, DriverInfo> getAll(){
        Map<Account, DriverInfo> drivers = new HashMap<>();

        for(Driver driver : driverList.getDrivers()){

            if(driver == null || driver.getDriver() == null || driver.getAccount() == null || driver.isClosed() || driver.getAccount().getUsername() == null){
                continue;
            }

            DriverInfo driverInfo = new DriverInfo();
            try {
                driverInfo.setUrl(driver.getDriver().getCurrentUrl());
            }catch (Exception e){}
            try {
                driverInfo.setTitle(driver.getDriver().getTitle());
            }catch (Exception e){}
            try {
                driverInfo.setH1(driver.getDriver().findElement(By.tagName("h1")).getText());
            }catch (Exception e){}
            try {
                driverInfo.setH2(driver.getDriver().findElement(By.tagName("h2")).getText());
            }catch (Exception e){}

            drivers.put(driver.getAccount(), driverInfo);
        }

        logger.info("REQUEST: get all accounts and drivers");
        return drivers;
    }

    @GetMapping(value = "/account-usernames")
    public List<String> accountUsernames(){
        List<String> usernames = new ArrayList<>();
        List<Account> accounts = driverList.getAccounts();
        for(Account account : accounts){
            usernames.add(account.getUsername());
        }
        logger.info("REQUEST: get all account usernames");
        return usernames;
    }

    @GetMapping(value = "/accounts")
    public List<Account> accounts(){
        logger.info("REQUEST: get all accounts");
        return driverList.getAccounts();
    }

    @GetMapping(value = "/get/{username}")
    public Map<Account, DriverInfo> get(@PathVariable String username){
        Account account = accountRepository.findByUsername(username);
        Map<Account, DriverInfo> drivers = new HashMap<>();

        Driver driver = driverList.get(account);

        DriverInfo driverInfo = new DriverInfo();
        try {
            driverInfo.setUrl(driver.getDriver().getCurrentUrl());
        }catch (Exception e){}
        try {
            driverInfo.setTitle(driver.getDriver().getTitle());
        }catch (Exception e){}
        try {
            driverInfo.setH1(driver.getDriver().findElement(By.tagName("h1")).getText());
        }catch (Exception e){}
        try {
            driverInfo.setH2(driver.getDriver().findElement(By.tagName("h2")).getText());
        }catch (Exception e){}

        drivers.put(account, driverInfo);

        logger.info("REQUEST: get account driver " + account.getUsername());
        return drivers;
    }

    @GetMapping(value = "/html/{username}")
    public String html(@PathVariable String username){
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            logger.info("REQUEST: Get html from driver for " + username + " || RESPONSE: " + false + " : account == null");
            return "account == null";
        }
        Driver driver = driverList.get(account);

        if(driver == null || driver.getDriver() == null){
            logger.info("REQUEST: Get html from driver for " + username + " || RESPONSE: " + false + " : driver == null");
            return "driver == null";
        }

        try {
            logger.info("REQUEST: Get html from driver for " + username + " || RESPONSE: " + true);
            return driver.getDriver().findElement(By.tagName("html")).getAttribute("innerHTML");
        }catch (Exception e){
            logger.info("REQUEST: Get html from driver for " + username + " || RESPONSE: " + false + " : failed returning html");
            e.printStackTrace();
            return "failed returning html";
        }
    }

    @GetMapping(value = "/size")
    public Integer size(){
        return driverList.getDrivers().size();
    }

    @GetMapping(value = "/running/{username}")
    public Boolean isAccountDriverRunning(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        boolean isRunning = driverList.contains(account);
        logger.info("REQUEST: Check if account" + username + "'s driver is running. || RESPONSE: " + isRunning);
        return isRunning;
    }

    @GetMapping(value = "/close/{username}")
    public Boolean closeDriverByUsername(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            logger.info("REQUEST: Close " + username + "'s driver. || RESPONSE: " + false + " (account does not exist)");
            return false;
        }

        Driver driver = driverList.get(account);
        if(driver == null || driver.getDriver() == null){
            logger.info("REQUEST: Close " + username + "'s driver. || RESPONSE: " + false + " (account does not have a driver)");
            return false;
        }

        try {
            driver.close();
        }catch (Exception e){
            logger.info("REQUEST: Close " + username + "'s driver. || RESPONSE: " + false + " (could not close driver)");
        }
        driverList.remove(account);

        logger.info("REQUEST: Close " + username + "'s driver. || RESPONSE: " + true);
        return true;
    }

    @GetMapping(value = "/loggedin/{username}")
    public Boolean isLoggedIn(@PathVariable String username) {
        Account account = accountRepository.findByUsername(username);
        if(account == null){
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
            return false;
        }

        Driver driver = driverList.get(account);
        if(driver == null || driver.getDriver() == null || driver.isClosed()){
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
            return false;
        }

        if(LogInMethods.isLoggedIn(driver)) {
            logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + true);
            return true;
        }

        logger.info("REQUEST: Is " + username + " logged into a driver. || RESPONSE: " + false);
        return false;
    }

    @GetMapping(value = "/contains/{username}")
    public Boolean contains(@PathVariable String username){
        Account account = accountRepository.findByUsername(username);
        return driverList.contains(account);
    }

    @GetMapping(value = "/goto/{username}")
    public Boolean gotoPage(@PathVariable String username, HttpServletRequest request){
        String url = request.getParameter("url");
        Account account = accountRepository.findByUsername(username);

        if(driverList.contains(account)){
            Driver driver = driverList.get(account);
            if(driver == null || driver.getDriver() == null || driver.isClosed()){
                logger.info("REQUEST: Go to page \"" + url + "\" by " + username + " || RESPONSE: " + false + " (driver == null or driver isClosed)");
                return false;
            }

            driver.getDriver().get(url);
            logger.info("REQUEST: Go to page \"" + url + "\" by " + username + " || RESPONSE: " + true);
            return true;
        }

        logger.info("REQUEST: Go to page \"" + url + "\" by " + username + " || RESPONSE: " + false + " (there is no driver)");
        return false;
    }




}
