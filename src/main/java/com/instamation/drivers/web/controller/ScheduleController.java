package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
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

    @Scheduled(cron="0 1 1 * * ?", zone="Europe/London")
    public void resetActions(){
        List<Account> accounts = accountRepository.findAll();

        for(Account account : accounts){
            account.setActions(0);
            account.getSetting().setPostActions(0);
        }

        accountRepository.saveAll(accounts);
    }

    @RequestMapping("/update-stats")
//    @Scheduled(cron="0 0 */4 * * *", zone="Europe/London")
    public void updateStats() throws Exception{
        logger.info("Updating stats for all account");
        List<Account> accounts = accountRepository.findByEnabled(true);

        for(Account account : accounts){
            Driver driver;

            if(DriverList.containsKey(account)){
                driver = DriverList.get(account);
            } else if(account.getProxy() != null){
                driver = new Driver(false, account.getProxy().getIp());
                DriverList.put(account, driver);
            } else {
                driver = new Driver();
                DriverList.put(account, driver);
            }

            try {
                Actions.login(driver, account);
            }catch (Exception e){
                logger.info(account.getUsername() + " failed to login");
            }
            try{
                Actions.updateProfileDetails(driver, account);
            }catch (Exception e){
                logger.info(account.getUsername() + " failed to updateProfileDetails");
            }
            try{
                account.updateStats(statsRepository);
            }catch (Exception e){
                logger.info(account.getUsername() + " failed to updateStats");
            }
            try{
                Actions.updateFollowers(driver, account, followerRepository);
            }catch (Exception e){
                logger.info(account.getUsername() + " failed to updateFollowers");
            }

            accountRepository.save(account);

            logger.info(account.getUsername() + " stats have been updated");
        }

        logger.info("Completed stats update - All account stats have been updated");

    }

    @Scheduled(cron="0 0 */1 * * *", zone="Europe/London")
    public void updateMemberships() throws Exception{
        List<Account> accounts = accountRepository.findAll();

        for(Account account : accounts){
            // check if today is after account expiry date and account is not pending an upgrade.
            if(new Date(System.currentTimeMillis()).after(account.getExpiryDate()) && !account.isPendingUpgrade()){
                account.setEnabled(false);
                sendRequest("https://insta-mation.com/automate/stop/" + account.getId());
            }

            // check if today is after account expiry date and account is pending an upgrade.
            if(new Date(System.currentTimeMillis()).after(account.getExpiryDate()) && account.isPendingUpgrade()){
                account.setEnabled(true);
                account.setPendingUpgrade(false);
            }

            accountRepository.save(account);
        }

    }

    private void sendRequest(String url) throws Exception{

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
    }

}
