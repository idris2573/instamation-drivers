package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.*;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.*;

@Controller
@Component
@RequestMapping(value = "/automate")
public class AutomationController {

    private static final Logger logger = Logger.getLogger(AutomationController.class);

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
    private ActionTypeRepository actionTypeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProfileSeedRepository profileSeedRepository;

//    @RequestMapping("/run")
//    @ResponseBody
    @Scheduled(fixedRate = 5000)
    public void run() throws Exception{

        List<Account> accounts = accountRepository.findByRunningAndEnabled(true, true);

        for(Account account : accounts){
            if(!account.isAutomationLock()) {
                account.setAutomationLock(true);
                accountRepository.save(account);

                try {
                    Setting setting = settingRepository.findByAccount(account);
                    List<Profile> profiles = profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false);

                    if (profiles.isEmpty()) {
                        logger.info(account.getUsername() + "has no profiles, adding new profiles.");

                        Driver driver;
                        if (DriverList.containsKey(account)) {
                            driver = DriverList.get(account);
                        } else if (account.getProxy() != null) {
                            driver = new Driver(false, account.getProxy().getIp());
                            DriverList.put(account, driver);
                            Actions.login(driver, account);
                        } else {
                            driver = new Driver();
                            DriverList.put(account, driver);
                            Actions.login(driver, account);
                        }

                        for (ProfileSeed profileSeed : profileSeedRepository.findByAccountAndUsed(account, false)) {
                            String prepend = (profileSeed.getType().equalsIgnoreCase("username")) ? "@" : "#";
                            logger.info(account.getUsername() + " adding profiles using '" + prepend + profileSeed.getName() + "' seed");

                            Actions.getProfiles(driver, account, profileSeed, profileRepository);

                            if (profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false).size() > 2000) {
                                break;
                            }

                            profileSeed.setUsed(true);
                            profileSeedRepository.save(profileSeed);

                            logger.info(account.getUsername() + " finished getting profiles");
                        }
                    }

                    // CHECK IF
                    // account is enabled
                    // account actions is less than actions per day
                    // accounts settings working time is true
                    // account HAS list of profile to work with
                    // accounts settings has 1 of the four actions active

                    if (account.isEnabled() && account.getActions() < setting.getActionsPerDay() && setting.isWorkingTime() && !profiles.isEmpty()
                            && (setting.isFollow() || setting.isLikes() || setting.isUnfollow() || setting.isComment())) {

                        Driver driver;
                        if (DriverList.containsKey(account)) {
                            driver = DriverList.get(account);
                        } else if (account.getProxy() != null) {
                            driver = new Driver(false, account.getProxy().getIp());
                            DriverList.put(account, driver);
                            Actions.login(driver, account);
                        } else {
                            driver = new Driver();
                            DriverList.put(account, driver);
                            Actions.login(driver, account);
                        }

                        logger.info(account.getUsername() + " starting automation");
                        DriverList.put(account, driver);


                        automate(account);
                    }
                }catch (Exception e){
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                }
            }
        }
    }

    @PostMapping(value = "/start")
    public String startRunning(@ModelAttribute Account account, RedirectAttributes redirectAttributes, HttpServletRequest request){

        logger.info(account.getUsername() + " has started automation");

        if(profileSeedRepository.findByAccount(accountRepository.findByUsername(account.getUsername())).isEmpty()){

            String referer = request.getHeader("referer");
            return "redirect:" + referer;
        }

        account = accountRepository.findByUsername(account.getUsername());

        if(account.getSetting().getActionSpeed().isEmpty()){
            account.getSetting().setActionSpeed("normal");
            account.getSetting().updateSettingsSpeed();
        }

        account.setRunning(true);
        accountRepository.save(account);


        String referer = request.getHeader("referer");
        return "redirect:" + referer;
    }

    @RequestMapping(value = "/stop")
    public String stopRunning(@ModelAttribute Account account, RedirectAttributes redirectAttributes, HttpServletRequest request) throws Exception{

        account = accountRepository.findByUsername(account.getUsername());
        account.setRunning(false);

//        int attempt = 0;
//
//        do{
//            Thread.sleep(1000);
//            if(DriverList.get(account) != null) {
//                DriverList.get(account).close();
//                DriverList.remove(account);
//                break;
//            } else {
//                attempt++;
//            }
//        }while (attempt <= 5);


        accountRepository.save(account);
        logger.info(account.getUsername() + " has stopped automation");

        String referer = request.getHeader("referer");
        return "redirect:" + referer;
    }

    @RequestMapping(value = "/stop-all")
    public String stopAllRunning(RedirectAttributes redirectAttributes, HttpServletRequest request){

        // TODO: STOP ALL AUTOMATION


        String referer = request.getHeader("referer");
        return "redirect:" + referer;
    }


    ///////////THE ACTUAL AUTOMATION/////////////////
    public void automate(Account account){
        Thread thread = new Thread() {
            @Override
            public void run() {

                Setting setting = settingRepository.findByAccount(account);
                Driver driver = DriverList.get(account);

                // Gets a list of profiles that have not been unfollowed
                List<Profile> profiles = profileRepository.findByAccountAndUnfollowed(account, false);

                // seperate into 2 lists actions and unfollow
                Queue<Profile> unfollowProfiles = new ArrayDeque<>();
                sanatizeAutomatedLists(profiles, unfollowProfiles, setting);

                int i = 0;

                for (Profile profile : profiles) {
                    try {

                        if(!accountRepository.findById(account.getId()).get().isRunning()){
                            logger.info(account.getUsername() + " has stopped automation");
                            break;
                        }

                        // If account has reached is actions per day limit or is not working, break the loop.
                        if (account.getActions() >= setting.getActionsPerDay() || !setting.isWorkingTime()) {
                            logger.info(account.getUsername() + " has stopped automation");
                            break;
                        }

                        // If all settings are off, break loop.
                        if (!setting.isFollow() && !setting.isLikes() && !setting.isUnfollow() && !setting.isComment()){
                            logger.info(account.getUsername() + " has stopped automation");
                            break;
                        }

                        // UNFOLLOW - Unfollow first because its polling from a queue.
                        if(setting.isUnfollow() && !unfollowProfiles.isEmpty()) {
                            Profile unfollowProfile = unfollowProfiles.poll();
                            if (unfollowProfile.isFollowing() && unfollowProfile.isUnfollowTime(setting)) {
                                driver.getDriver().get("https://instagram.com/" + unfollowProfile.getUsername());
                                Actions.unfollowProfile(driver, unfollowProfile);
                                account.setActions(account.getActions() + 1);
                                unfollowProfile.setFollowing(false);
                                unfollowProfile.setUnfollowed(true);
                                profileRepository.save(unfollowProfile);
                                accountRepository.save(account);

                                Action action = new Action();
                                action.setAccount(account);
                                action.setActionType(getActionType("UNFOLLOW"));
                                action.setDescription(account.getUsername() + " unfollowed " + profile.getUsername());
                                logger.info(account.getUsername() + " unfollowed " + profile.getUsername());
                                actionRepository.save(action);

                                Thread.sleep(setting.actionSleepTime());
                            }
                        }

                        driver.getDriver().get("https://instagram.com/" + profile.getUsername());

                        if(Actions.isNotAvailable(driver)){
                            continue;
                        }

                        // FOLLOW
                        if (setting.isFollow() && !profile.isFollowing()) {
                            Actions.followProfile(driver, profile);
                            account.setActions(account.getActions() + 1);
                            profile.setFollowing(true);
                            profile.setFollowDate(new Date(System.currentTimeMillis()));
                            profileRepository.save(profile);
                            accountRepository.save(account);

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("FOLLOW"));
                            action.setDescription(account.getUsername() + " followed " + profile.getUsername());
                            logger.info(account.getUsername() + " followed " + profile.getUsername());
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                        }

                        if (Actions.isPrivate(driver)) {
                            continue;
                        }

                        // LIKE POSTS
                        if (setting.isLikes() && !profile.isLiked()) {
                            Actions.likeProfilePosts(driver, profile, setting);
                            account.setActions(account.getActions() + 1);
                            profile.setLiked(true);
                            profileRepository.save(profile);
                            accountRepository.save(account);

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("LIKE"));
                            action.setDescription(account.getUsername() + " liked " + profile.getUsername() + "'s post");
                            logger.info(account.getUsername() + " liked " + profile.getUsername() + "'s post");
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                        }

                        // COMMENT
                        if (setting.isComment() && !profile.isCommented() && !commentRepository.findByAccount(account).isEmpty()) {
                            Actions.commentProfilePosts(driver, profile, commentRepository.findRandom());
                            account.setActions(account.getActions() + 1);
                            profile.setCommented(true);
                            profileRepository.save(profile);
                            accountRepository.save(account);

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("COMMENT"));
                            action.setDescription(account.getUsername() + " commented on " + profile.getUsername() + "'s post");
                            logger.info(account.getUsername() + " commented on " + profile.getUsername() + "'s post");
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                        }

                        if(i % 30 == 0){
                            updateStats(driver, account);
                        }

                        i++;
                    } catch (Exception e) {
//                        System.out.println("Driver has been closed");
                        driver = DriverList.get(account);
//                        drivers.remove(account);
                    }
                }

                if(driver != null) {
                    logger.info(account.getUsername() + " has stopped automation");
                }

                account.setAutomationLock(false);
                accountRepository.save(account);
            }
        };
        thread.start();
    }

    private ActionType getActionType(String type){
        return actionTypeRepository.findByType(type);
    }

    private void sanatizeAutomatedLists(List<Profile> profiles, Queue<Profile> unfollowProfiles, Setting setting){
        List<Profile> removeProfiles = new ArrayList<>();

        for(Profile profile : profiles){
            // Add profile to unfollow list and remove profile from main profile list if profile is at unfollow time
            if(profile.isFollowing() && profile.getFollowDate() != null && profile.isUnfollowTime(setting)){
                unfollowProfiles.add(profile);
                removeProfiles.add(profile);
            }

            // Remove profile from list if profile is already followed.
            if(profile.isFollowing()){
                removeProfiles.add(profile);
            }
        }

        for(Profile profile : removeProfiles) {
            profiles.remove(profile);
        }
    }


    private void updateStats(Driver driver, Account account){
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


}
