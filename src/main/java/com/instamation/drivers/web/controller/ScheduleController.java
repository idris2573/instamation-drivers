package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
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

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.protocol.HTTP.USER_AGENT;

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

    @Scheduled(cron="0 0 */5 * * *", zone="Europe/London")
    public void deleteUnused() throws Exception{
        List<Driver> deleteDrivers = new ArrayList<>();

        for(Driver driver : DriverList.getNewDrivers()){
            if(!driver.getAccount().getSetting().isWorkingTime() && !driver.getAccount().isAutomationLock()) {
                if (!DriverList.contains(driver) || !DriverList.driversContainNewDriver(driver)) {
                    logger.info(driver.getAccount().getUsername() + " is being removed from new Drivers");
                    driver.close();
                    deleteDrivers.add(driver);
                }
            }
        }

        for(Driver driver : deleteDrivers){
            DriverList.getNewDrivers().remove(driver);
        }
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
                if(DriverList.containsKey(account)){
                    driver = DriverList.get(account);
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
                    Actions.updateProfileDetails(driver, account);
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

                logger.info(account.getUsername() + " stats have been updated");

                if(!isLoggedIn) {
                    driver.close();
                }
            }

        }

        logger.info("Completed stats update - All account stats have been updated");

    }
}
