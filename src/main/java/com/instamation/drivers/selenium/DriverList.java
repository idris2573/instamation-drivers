package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverList {

    private static Map<Account, Driver> drivers = new HashMap<>();
    private static List<Driver> newDrivers = new ArrayList<>();

    public static Map<Account, Driver> getDrivers() {
        return drivers;
    }

    public static boolean containsKey(Account account){
        for(Map.Entry driver : drivers.entrySet()){
            Account accountEntry = (Account) driver.getKey();
            if(account.getId().equals(accountEntry.getId())){
                return true;
            }
        }
        return false;
    }

    public static Driver get(Account account){
        if(account == null){
            return null;
        }
        for(Map.Entry driver : drivers.entrySet()){
            Account accountEntry = (Account) driver.getKey();
            if(account.getId().equals(accountEntry.getId())){
                return (Driver) driver.getValue();
            }
        }
        return null;
    }

    public static boolean contains(Driver driver){
        if(driver == null){
            return false;
        }
        for(Map.Entry driverMap : drivers.entrySet()){
            Driver driverEntry = (Driver) driverMap.getValue();
            if(driverEntry == driver || driverEntry.equals(driver)){
                return true;
            }
        }
        return false;
    }

    public static void remove(Account account){
        for(Map.Entry driver : drivers.entrySet()){
            Account accountEntry = (Account) driver.getKey();
            if(account.getId().equals(accountEntry.getId())){
                drivers.remove(accountEntry);
            }
        }
    }

    public static void put(Account account, Driver driver){
        for(Map.Entry driver1 : drivers.entrySet()){
            Account accountEntry = (Account) driver1.getKey();
            if(account.getId().equals(accountEntry.getId())){
                drivers.put(accountEntry, driver);
            }
        }
        drivers.put(account, driver);
    }

    public static List<Driver> getNewDrivers() {
        return newDrivers;
    }
}
