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

    @Scheduled(cron="0 0 */4 * * *", zone="Europe/London")
    public void deleteUnused() throws Exception{
        List<Driver> deleteDrivers = new ArrayList<>();

        for(Driver driver : DriverList.getNewDrivers()){
            if(!DriverList.contains(driver)){
                driver.close();
                deleteDrivers.add(driver);
            }
        }

        for(Driver driver : deleteDrivers){
            DriverList.getNewDrivers().remove(driver);
        }
    }

}
