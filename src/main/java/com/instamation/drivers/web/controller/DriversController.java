package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(value = "/drivers")
public class DriversController {

    @CrossOrigin(origins = "http://localhost:8081")
    @GetMapping(value = "/all")
    public Set<Account> getAll(){
        Set<Account> accounts = new HashSet<>(DriverList.getDrivers().keySet());
        return accounts;
    }
}
