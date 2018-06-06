package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Action;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.selenium.LogInMethods;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@Component
public class ScheduleController {

    private static final Logger logger = Logger.getLogger(ScheduleController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private DriverList driverList;

    @Scheduled(cron="0 25 */2 * * *", zone="Europe/London")
    public void deleteNullDrivers() throws Exception{
        logger.info("Deleting null drivers...");

        List<Driver> deleteDrivers = new ArrayList<>();
        for(Driver driver : driverList.getDrivers()){

            // driver is not ready
            // drivers account is not enabled
            // driver is on unusal account enter code page
            if (!driverList.isDriverReady(driver) || !driver.getAccount().isEnabled() || driver.getDriver().getCurrentUrl().contains("com/challenge/") || Actions.isNotAvailable(driver)) {
                Account account = accountRepository.findByUsername(driver.getAccount().getUsername());
                try {
                    account.setLoggedIn(false);
                    account.setRunning(false);
                    accountRepository.save(account);
                }catch (Exception e){}

                driver.setAccount(account);
                driverList.save(driver);
                deleteDrivers.add(driver);
            }
        }

        for(Driver driver : deleteDrivers){
            driver.close();
            driverList.remove(driver);
        }

        driverList.deleteUnusedPids();
    }

    @RequestMapping("/check-logged-in")
    @Scheduled(cron="0 15 */1 * * *", zone="Europe/London")
    public void checkLoggedIn(){
        logger.info("Checking logged in drivers...");
        List<Driver> deleteDrivers = new ArrayList<>();

        for(Driver driver : driverList.getDrivers()){
            Account account = accountRepository.findByUsername(driver.getAccount().getUsername());

            if(!driver.getAccount().isAutomationLock()){

                // check if driver is logged in
                if(LogInMethods.isLoggedIn(driver)) {
                    account.setLoggedIn(true);
                    logger.info("setting " + driver.getAccount().getUsername() + " as logged in");
                } else {
                    account.setLoggedIn(false);
                    account.setRunning(false);
                    deleteDrivers.add(driver);
                    logger.info("setting " + driver.getAccount().getUsername() + " as logged out and removing driver");
                }

                driver.setAccount(account);
                driverList.save(driver);
                accountRepository.save(account);
            }
        }

        for(Driver driver : deleteDrivers){
            driver.close();
            driverList.remove(driver);
        }

        driverList.deleteUnusedPids();
    }

    @RequestMapping("/update-stats")
    @Scheduled(cron="0 0 */4 * * *", zone="Europe/London")
    public void updateStats() throws Exception{
        logger.info("Updating stats for all accounts");
        List<Account> accounts = accountRepository.findByEnabled(true);
        Driver driver;
        Boolean isLoggedIn;

        for(Account account : accounts) {
            if (!account.getSetting().isWorkingTime() && !account.isAutomationLock()) {

                // check if account has a driver and is logged in.
                if(driverList.contains(account)){
                    driver = driverList.get(account);
                    if(LogInMethods.isLoggedIn(driver)){
                        isLoggedIn = true;
                        account.setLoggedIn(true);
                    } else {
                        isLoggedIn = false;
                    }
                } else {
                    isLoggedIn = false;
                    driver = new Driver(account);
                    account.setLoggedIn(false);
                }

                if(!driverList.isDriverReady(driver)){
                    logger.error(account.getUsername() + " driver is not ready");
                    continue;
                }

                // check if account is available, if not skip account
                driver.getDriver().get("https://instagram.com/" + account.getUsername());
                if(Actions.isNotAvailable(driver)){
                    account.setAvailable(false);
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    accountRepository.save(account);
                    logger.info(account.getUsername() + " is unavaliable");
                    continue;
                }

                logger.info(account.getUsername() + " isLoggedIn = " + isLoggedIn);

                try {
                    Actions.updateProfileDetails(driver, account, accountRepository);
                } catch (Exception e) {
                    logger.info(account.getUsername() + " failed to updateProfileDetails");
                }

                if(isLoggedIn) {
                    try {
                        Actions.updateFollowers(driver, account, followerRepository);
                    } catch (Exception e) {
                        logger.info(account.getUsername() + " failed to updateFollowers");
                    }
                }

                try {
                    account.updateStats(statsRepository);
                } catch (Exception e) {
                    logger.info(account.getUsername() + " failed to updateStats");
                }

                accountRepository.save(account);
                driver.setAccount(account);
                driverList.save(driver);

                logger.info(account.getUsername() + " stats have been updated");

                if(!isLoggedIn) {
                    driver.close();
                }
            }

        }

        logger.info("Completed stats update - All account stats have been updated");

    }

}
