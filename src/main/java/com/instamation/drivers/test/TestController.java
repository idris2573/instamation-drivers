package com.instamation.drivers.test;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.repository.ProxyRepository;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.selenium.StaticMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private DriverList driverList;

    @RequestMapping(value = "/start")
    @ResponseBody
    public String start() throws Exception{

//        Driver driver = new Driver();
//        DriverList.save(accountRepository.findById(1L).get(), driver);
        return "ok";
    }

    @PostMapping(value = "/start2")
    @ResponseBody
    public String start2() throws Exception{

//        Driver driver = new Driver();
//        DriverList.save(accountRepository.findById(1L).get(), driver);
        return "ok";
    }


    @RequestMapping(value = "/drivers")
    public List<Driver> drivers(){
        return driverList.getDrivers();
    }


    @RequestMapping(value = "/accounts")
    public List<Account> accounts(){
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

    @RequestMapping(value = "/account")
    public Account account(){
        return accountRepository.findById(1L).get();
    }

    @RequestMapping(value = "/equals")
    public boolean equals() throws Exception{
        Account account = new Account();
        account.setUsername("Idris");
        account.setId(1L);
        Driver driver = new Driver(account);
        driverList.save(driver);

        Account account2 = new Account();
        account2.setUsername("Honesty");
        account2.setId(2L);
        Driver driver2 = new Driver(account2);
        driverList.save(driver2);

        return driver.equals(driver2);
    }


    @RequestMapping("/pid")
    @ResponseBody
    public boolean pid() throws Exception {

        Account account = new Account();
        account.setId(1L);
        account.setUsername("TeddyRae");

        List<String> pids = StaticMethods.checkChromeProcessPIDList();
        // get list of pids and save in array
        Driver driver = new Driver(account);
        List<String> accountPids = StaticMethods.checkChromeProcessPIDList();
        for(String pid : pids){
            accountPids.remove(pid);
        }
        driver.setPids(accountPids);
        driver.setPids(null);

        return true;
    }

    @RequestMapping(value = "/stats")
    public boolean stats() throws Exception{
        Account account = new Account();
        account.setUsername("Idris");
        account.setId(1L);
        Driver driver = new Driver(account);
        driverList.save(driver);

        driver.getDriver().get("https://instagram.com/Sadaf_islam_official");
        Actions.updateProfileDetails(driver,account,accountRepository);


        return true;
    }

}
