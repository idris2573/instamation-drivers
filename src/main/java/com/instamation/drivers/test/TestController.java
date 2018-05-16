package com.instamation.drivers.test;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.repository.ProxyRepository;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @RequestMapping(value = "/start")
    @ResponseBody
    public String start() throws Exception{

        Driver driver = new Driver();
        DriverList.put(accountRepository.findById(1L).get(), driver);
        return "ok";
    }

    @PostMapping(value = "/start2")
    @ResponseBody
    public String start2() throws Exception{

        Driver driver = new Driver();
        DriverList.put(accountRepository.findById(1L).get(), driver);
        return "ok";
    }


    @RequestMapping(value = "/drivers")
    @ResponseBody
    public Map<Account, Driver> drivers(){
        return DriverList.getDrivers();
    }


    @RequestMapping(value = "/accounts")
    @ResponseBody
    public List<Account> accounts(){
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

    @RequestMapping(value = "/account")
    @ResponseBody
    public Account account(){
        return accountRepository.findById(1L).get();
    }

}
