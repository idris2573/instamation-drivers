package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Response;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/drivers")
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
public class DriversController {

    @GetMapping(value = "/all")
    public Set<Account> getAll(){
        Set<Account> accounts = new HashSet<>(DriverList.getDrivers().keySet());
        return accounts;
    }

    @GetMapping(value = "/running/{account}")
    public Boolean isAccountDriverRunning(@RequestBody Account account) {
        if (DriverList.get(account) != null){
            return true;
        } else{
            return false;
        }
    }

    @GetMapping(value = "/running2/{account}")
    public Response isAccountDriverRunning2(@RequestBody Account account) {
        if (DriverList.get(account) != null){
            return new Response("true");
        } else{
            return new Response("false");
        }
    }


}
