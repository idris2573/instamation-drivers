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

    @Scheduled(fixedRate = 5000)
    public void run() throws Exception{

        List<Account> accounts = accountRepository.findByRunningAndEnabled(true, true);

        for(Account account : accounts){

            if(!account.isAutomationLock() && account.getSetting().isWorkingTime()) {
                Driver driver = DriverList.get(account);
                account.setAutomationLock(true);
                accountRepository.save(account);

                // if the account does not have a driver or is not logged in,
                // set the accounts automation off. Also set the account as not logged in.
                if(driver == null || !account.isLoggedIn()){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                    continue;
                }

                // if account is not available its been deleted or blocked by instagram
                driver.getDriver().get("https://instagram.com/" + account.getUsername());
                if(Actions.isNotAvailable(driver)){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                }

                // if account is not logged in, skip account and set logged in and running as false.
                driver.getDriver().get("https://instagram.com");
                if(Actions.doesButtonExist(driver, "Log In")){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                    continue;
                }

                // get profiles using profile seeds
                try {
                    Setting setting = settingRepository.findByAccount(account);
                    List<Profile> profiles = profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false);

                    if (profiles.isEmpty()) {
                        logger.info(account.getUsername() + "has no profiles, adding new profiles.");

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
                    logger.info(account.getUsername() + String.format("'s automation settings: \nenabled: %s, \nactions available: %s, \nis working time: %s, \nprofile list is not empty: %s, \nFULC action: %s",
                            account.isEnabled(),
                            (account.getActions() < setting.getActionsPerDay()),
                            setting.isWorkingTime(),
                            !profiles.isEmpty(),
                            (setting.isFollow() || setting.isLikes() || setting.isUnfollow() || setting.isComment())));

                    if (account.isEnabled() && account.getActions() < setting.getActionsPerDay() && setting.isWorkingTime() && !profiles.isEmpty()
                            && (setting.isFollow() || setting.isLikes() || setting.isUnfollow() || setting.isComment())) {

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

                        if(i % 30 == 0) {
                            updateStats(driver, account);

                            // if account is not logged in, skip account and set logged in and running as false.
                            driver.getDriver().get("https://instagram.com");
                            if (Actions.doesButtonExist(driver, "Log In")) {
                                account.setRunning(false);
                                account.setLoggedIn(false);
                                accountRepository.save(account);
                                break;
                            }

                            // check if user is not available
                            driver.getDriver().get("https://instagram.com/" + account.getUsername());
                            if(Actions.isNotAvailable(driver)){
                                account.setRunning(false);
                                account.setLoggedIn(false);
                                accountRepository.save(account);
                                break;
                            }
                        }

                        i++;

                        // if account is not running.
                        if(!accountRepository.findById(account.getId()).get().isRunning()){
                            break;
                        }

                        // If account has reached is actions per day limit or is not working, break the loop.
                        if (account.getActions() >= setting.getActionsPerDay() || !setting.isWorkingTime()) {
                            break;
                        }

                        // If all settings are off, break loop.
                        if (!setting.isFollow() && !setting.isLikes() && !setting.isUnfollow() && !setting.isComment()){
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

                    } catch (Exception e) {
//                        System.out.println("Driver has been closed");
                        driver = DriverList.get(account);
//                        drivers.remove(account);
                    }
                }

                logger.info(account.getUsername() + " has stopped automation");
                account.setAutomationLock(false);
                accountRepository.save(account);
            }
        };
        thread.start();
    }
    ///////////THE ACTUAL AUTOMATION/////////////////

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
