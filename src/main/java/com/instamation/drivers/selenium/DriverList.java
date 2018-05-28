package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.web.controller.ScheduleController;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverList {

    private static final Logger logger = Logger.getLogger(DriverList.class);

    private static Map<Account, Driver> drivers = new HashMap<>();
    private static List<Driver> newDrivers = new ArrayList<>();

    public static Map<Account, Driver> getDrivers() {
        return drivers;
    }

    public static boolean containsKey(Account account){
        if(account == null || account.getId() == null){
            return false;
        }

        for(Map.Entry driver : drivers.entrySet()){
            Account accountEntry = (Account) driver.getKey();
            if(account.getId().equals(accountEntry.getId())){
                return true;
            }
        }
        return false;
    }

    public static Driver get(Account account){
        if(account == null || account.getId() == null){
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
        if(account == null || account.getId() == null){
            return;
        }

        for(Map.Entry driver : drivers.entrySet()){
            Account accountEntry = (Account) driver.getKey();
            if(account.getId().equals(accountEntry.getId())){
                logger.info(account.getUsername() + " is being removed from DriverList");
                drivers.remove(accountEntry);
            }
        }
    }

    public static void put(Account account, Driver driver){
        if(account == null || account.getId() == null || driver == null){
            return;
        }

        for(Map.Entry driver1 : drivers.entrySet()){
            Account accountEntry = (Account) driver1.getKey();
            if(account.getId().equals(accountEntry.getId())){
                logger.info(account.getUsername() + " is getting updated in DriverList");
                drivers.put(accountEntry, driver);
                return;
            }
        }

        logger.info(account.getUsername() + " is being added to the DriverList");
        drivers.put(account, driver);
    }

//    #######################NEW DRIVERS#######################

    public static List<Driver> getNewDrivers() {
        return newDrivers;
    }

    public static boolean newDriversContain(Driver driver){
        if(driver == null || driver.isClosed()){
            return false;
        }

        if(driver.getAccount() == null){
            return false;
        }

        for(Driver driver1 : newDrivers){
            if(driver1.getAccount().getUsername().equalsIgnoreCase(driver.getAccount().getUsername())){
                return true;
            }
        }

        return false;
    }

    public static boolean driversMapContainNewDriver(Driver driver){
        if(driver == null || driver.isClosed()){
            return false;
        }

        if(driver.getAccount() == null){
            return false;
        }

        for(Map.Entry driverMap : drivers.entrySet()){
            Driver driverEntry = (Driver) driverMap.getValue();
            if(driverEntry.getAccount().getUsername().equalsIgnoreCase(driver.getAccount().getUsername())){
                return true;
            }
        }

        return false;
    }

    public static Driver getNewDriver(Account account){
        if(account == null || newDrivers.isEmpty()){
            return null;
        }
        for(Driver driver : newDrivers){
            if(driver.getAccount() != null && driver.getAccount().getUsername().equalsIgnoreCase(account.getUsername())){
                logger.info(account.getUsername() + " is getting a NEW driver after loggin error");
                return driver;
            }
        }
        return null;
    }
}
