package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverList {

    private static final Logger logger = Logger.getLogger(DriverList.class);

    private List<Driver> drivers = new ArrayList<>();

    public Driver get(Account account){
        if(account == null || (account.getId() == null && account.getUsername() == null)){
            return null;
        }
        for(Driver driver : drivers){
            if(account.equals(driver.getAccount())){
                return driver;
            }
        }
        return null;
    }

    public boolean contains(Account account){
        if(account == null || (account.getId() == null && account.getUsername() == null)){
            return false;
        }
        for(Driver driver : drivers){
            if(account.equals(driver.getAccount())){
                return true;
            }
        }
        return false;
    }

    public boolean contains(Driver driver){
        if(driver == null || driver.getDriver() == null || driver.getAccount() == null){
            return false;
        }
        for(Driver driverEntry : drivers){
            if(driverEntry.equals(driver)){
                return true;
            }
        }
        return false;
    }

    public void remove(Account account){
        if(account == null || (account.getId() == null && account.getUsername() == null)){
            return;
        }
        for(Driver driver : drivers){
            if(account.equals(driver.getAccount())){
                logger.info(account.getUsername() + " is being removed from DriverList");
                driver.close();
                drivers.remove(driver);
                return;
            }
        }
    }

    public void remove(Driver driver){
        if(driver == null || driver.getDriver() == null || driver.getAccount() == null){
            return;
        }
        for(Driver driverEntry : drivers){
            if(driver.equals(driverEntry)){
                logger.info(driver.getAccount().getUsername() + " is being removed from DriverList");
                driver.close();
                drivers.remove(driverEntry);
                return;
            }
        }
    }

    public void save(Driver driver){
        if(driver == null || driver.getDriver() == null || driver.getAccount() == null || (driver.getAccount().getId() == null && driver.getAccount().getUsername() == null)){
            return;
        }

        for(Driver driverEntry : drivers){
            if(driver.equals(driverEntry)){
                logger.info(driver.getAccount().getUsername() + " has been updated in DriverList");
                drivers.remove(driverEntry);
                drivers.add(driver);
                return;
            }
        }

        logger.info(driver.getAccount().getUsername() + " has been added to the DriverList");
        drivers.add(driver);
    }

    public List<Driver> getDrivers() {
        return drivers;
    }

    public List<Account> getAccounts(){
        List<Account> accounts = new ArrayList<>();
        for(Driver driver : drivers){
            accounts.add(driver.getAccount());
        }
        return accounts;
    }

    public boolean isDriverReady(Driver driver){
        if(driver == null || driver.getDriver() == null || driver.isClosed() || driver.getAccount() == null){
            return false;
        }

        return true;
    }

    public void deleteUnusedPids(){
        List<String> checkChromeProcessPIDList = StaticMethods.checkChromeProcessPIDList();
        List<String> allPids = getAllPids();

        for(String pid : checkChromeProcessPIDList){
            if(!allPids.contains(pid)){
                try{
                    String cmd;
                    if(System.getProperty("os.name").equals("Linux")) {
                        cmd = "kill " + pid;
                    } else {
                        cmd = "taskkill /F /PID " + pid;
                    }
                    Runtime.getRuntime().exec(cmd);
                    logger.info("killing PID " + pid);
                }catch (Exception e){}
            }
        }
    }

    public List<String> getAllPids(){
        List<String> allPids = new ArrayList<>();
        for(Driver driver : drivers){
            if(driver.getPids() != null) {
                for(String pid : driver.getPids()){
                    allPids.add(pid);
                }
            }
        }
        return allPids;
    }
}
