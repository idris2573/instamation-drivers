package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.*;
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
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private DriverList driverList;

    @Scheduled(fixedRate = 5000)
    public void run() throws Exception{

        List<Account> accounts = accountRepository.findByRunningAndEnabled(true, true);
        Driver driver;

        for(Account account : accounts){

            if(!account.isAutomationLock() && account.getSetting().isWorkingTime()) {
                logger.info("Checking " + account.getUsername() + " for automation");
                account.setAutomationLock(true);
                accountRepository.save(account);

                driver = driverList.get(account);

                // if the account does not have a driver or is not logged in,
                // set the accounts automation off. Also set the account as not logged in.
                if(driver == null || driver.getDriver() == null){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                    logger.info(account.getUsername() + "account does not have a driver, skipping automation..." );
                    continue;
                }

                // if account is not logged in, skip account and set logged in and running as false.
                if(!LogInMethods.isLoggedIn(driver)){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                    logger.info(account.getUsername() + " is not logged in, skipping automation...");
                    continue;

                }

                // if account is not available its been deleted or blocked by instagram
                driver.getDriver().get("https://instagram.com/" + account.getUsername());
                if(Actions.isNotAvailable(driver)){
                    account.setRunning(false);
                    account.setLoggedIn(false);
                    account.setAutomationLock(false);
                    account.setAvailable(false);
                    accountRepository.save(account);
                    logger.info(account.getUsername() + " is not available, skipping automation...");
                    continue;
                }

                account.setLoggedIn(true);
                accountRepository.save(account);

                Setting setting = settingRepository.findByAccount(account);
                List<Profile> profiles = profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false);

                // get profiles using profile seeds
                getProfileSeeds(account, driver, profiles);

                if(profiles.isEmpty()) {
                    profiles = profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false);
                }

                try {
                    // CHECK IF
                    // account is enabled
                    // account actions is less than actions per day
                    // accounts settings working time is true
                    // account HAS list of profile to work with
                    // accounts settings has 1 of the four actions active
                    logger.info(account.getUsername() + String.format("'s automation settings: \nusername: %s \nenabled: %s, \nactions available: %s, \nisWorkingTime: %s, \nprofile list notEmpty: %s, \nFULC action: %s",
                            account.getUsername(),
                            account.isEnabled(),
                            (account.getActions() < setting.getActionsPerDay()),
                            setting.isWorkingTime(),
                            !profiles.isEmpty(),
                            (setting.isFollow() || setting.isLikes() || setting.isUnfollow() || setting.isComment())));

                    // if account passes all automation checks
                    if (account.isEnabled() && account.getActions() < setting.getActionsPerDay() && setting.isWorkingTime() && !profiles.isEmpty()
                            && (setting.isFollow() || setting.isLikes() || setting.isUnfollow() || setting.isComment())) {

                        logger.info(account.getUsername() + " has passed all checks and is starting automation");
                        automate(account);
                    }
                }catch (Exception e){
                    logger.info(account.getUsername() + " ran into an error on automation checks, skipping...");
                    account.setAutomationLock(false);
                    accountRepository.save(account);
                }
            }
        }
    }

    ///////////THE ACTUAL AUTOMATION/////////////////
    public void automate(Account runAccount){
        Thread thread = new Thread() {
            @Override
            public void run() {

                Account account = runAccount;
                Setting setting = settingRepository.findByAccount(account);
                Driver driver = driverList.get(account);

                // Gets a list of profiles that have not been unfollowed
                List<Profile> profiles = profileRepository.findByAccountAndUnfollowed(account, false);

                // seperate into 2 lists actions and unfollow
                Queue<Profile> unfollowProfiles = new ArrayDeque<>();
                sanatizeAutomatedLists(profiles, unfollowProfiles, setting);

                int i = 0;

                for (Profile profile : profiles) {
                    try {

                        // if account is not running.
                        if(!isAccountRunning(account, driver)){
                            break;
                        }

                        if(i % 30 == 0) {
                            updateStats(driver, account);
                        }
                        i++;


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

                                if(!isAccountRunning(account, driver)){
                                    break;
                                }

                                Action action = new Action();
                                action.setAccount(account);
                                action.setActionType(getActionType("UNFOLLOW"));
                                action.setDescription(account.getUsername() + " unfollowed " + profile.getUsername());
                                logger.info(account.getUsername() + " unfollowed " + profile.getUsername());
                                actionRepository.save(action);

                                Thread.sleep(setting.actionSleepTime());
                                account = accountRepository.findById(account.getId()).get();
                            }
                        }

                        driver.getDriver().get("https://instagram.com/" + profile.getUsername());

                        if(Actions.isNotAvailable(driver)){
                            profile.setUnfollowed(true);
                            profileRepository.save(profile);
                            continue;
                        }

                        // FOLLOW
                        if (setting.isFollow() && !profile.isFollowing()) {
                            Actions.followProfile(driver, profile);
                            account.setActions(account.getActions() + 1);
                            profile.setFollowing(true);
                            profile.setFollowDate(new Date(System.currentTimeMillis()));
                            profileRepository.save(profile);

                            if(!isAccountRunning(account, driver)){
                                break;
                            }
                            driver.getDriver().get("https://instagram.com/" + profile.getUsername());

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("FOLLOW"));
                            action.setDescription(account.getUsername() + " followed " + profile.getUsername());
                            logger.info(account.getUsername() + " followed " + profile.getUsername());
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                            account = accountRepository.findById(account.getId()).get();
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

                            if(!isAccountRunning(account, driver)){
                                break;
                            }
                            driver.getDriver().get("https://instagram.com/" + profile.getUsername());

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("LIKE"));
                            action.setDescription(account.getUsername() + " liked " + profile.getUsername() + "'s post");
                            logger.info(account.getUsername() + " liked " + profile.getUsername() + "'s post");
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                            account = accountRepository.findById(account.getId()).get();
                        }

                        // COMMENT
                        if (setting.isComment() && !profile.isCommented() && !commentRepository.findByAccount(account).isEmpty()) {
                            Actions.commentProfilePosts(driver, profile, commentRepository.findRandom());
                            account.setActions(account.getActions() + 1);
                            profile.setCommented(true);
                            profileRepository.save(profile);

                            if(!isAccountRunning(account, driver)){
                                break;
                            }
                            driver.getDriver().get("https://instagram.com/" + profile.getUsername());

                            Action action = new Action();
                            action.setAccount(account);
                            action.setActionType(getActionType("COMMENT"));
                            action.setDescription(account.getUsername() + " commented on " + profile.getUsername() + "'s post");
                            logger.info(account.getUsername() + " commented on " + profile.getUsername() + "'s post");
                            actionRepository.save(action);

                            Thread.sleep(setting.actionSleepTime());
                            account = accountRepository.findById(account.getId()).get();
                        }

                    } catch (Exception e) {
                        driver = driverList.get(account);
                    }
                }

                account.setAutomationLock(false);
                logger.info(account.getUsername() + " has stopped automation");
                accountRepository.save(account);
            }
        };
        thread.start();
    }
    ///////////THE ACTUAL AUTOMATION/////////////////


    // #####################PRIVATE METHODS#######################
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
        logger.info(account.getUsername() + " updating stats (automation)");

        try{
            Actions.updateProfileDetails(driver, account, accountRepository);
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

    private boolean isAccountRunning(Account account, Driver driver) throws Exception{

        Account accountUpdated = accountRepository.findById(account.getId()).get();

        if(!driverList.isDriverReady(driver)){
            logger.error(account.getUsername() + " driver is not ready");
            return false;
        }

        // if account is not running.
        if(!accountUpdated.isRunning()){
            account.setRunning(false);
            accountRepository.save(account);
            logger.info(account.getUsername() + " has stopped running");
            return false;
        }

        Setting setting = account.getSetting();

        // If account has reached is actions per day limit or is not working, break the loop.
        if (account.getActions() >= setting.getActionsPerDay()) {
            accountRepository.save(account);
            logger.info(account.getUsername() + " has ran out of actions");
            return false;
        }

        if(!setting.isWorkingTime()){
            accountRepository.save(account);
            logger.info(account.getUsername() + " has completed working time");
            return false;
        }

        Setting settingUpdate = accountUpdated.getSetting();

        setting.setLikes(settingUpdate.isLikes());
        setting.setComment(settingUpdate.isComment());
        setting.setFollow(settingUpdate.isFollow());
        setting.setUnfollow(settingUpdate.isUnfollow());
        setting.setActionSpeed(settingUpdate.getActionSpeed());
        setting.setMediaType(settingUpdate.getMediaType());
        setting.setMinLikesFilter(settingUpdate.getMinLikesFilter());
        setting.setMaxLikesFilter(settingUpdate.getMaxLikesFilter());

        // If all settings are off, break loop.
        if (!setting.isFollow() && !setting.isLikes() && !setting.isUnfollow() && !setting.isComment()){
            accountRepository.save(account);
            logger.info(account.getUsername() + " has turned all actions off");
            return false;
        }

        // if account is not logged in, skip account and set logged in and running as false.
        if (!LogInMethods.isLoggedIn(driver)) {
            setRunningFalse(account);
            logger.info(account.getUsername() + " is not logged in");
            return false;
        }

        // check if user is not available
        driver.getDriver().get("https://instagram.com/" + account.getUsername());
        if(Actions.isNotAvailable(driver)){
            account.setAvailable(false);
            setRunningFalse(account);
            logger.info(account.getUsername() + " is not available");
            return false;
        }

        account.setLoggedIn(true);
        accountRepository.save(account);
        return true;
    }

    private void setRunningFalse(Account account){
        account.setRunning(false);
        account.setLoggedIn(false);
        accountRepository.save(account);
    }

    private void getProfileSeeds(Account account, Driver driver, List<Profile> profiles) throws Exception{
        if (profiles.isEmpty()) {
            logger.info(account.getUsername() + "has no profiles, adding new profiles.");

            List<ProfileSeed> profileSeeds = profileSeedRepository.findByAccountAndUsed(account, false);
            String prepend;

            for (ProfileSeed profileSeed : profileSeeds) {
                prepend = (profileSeed.getType().equalsIgnoreCase("username")) ? "@" : "#";
                logger.info(account.getUsername() + " adding profiles using '" + prepend + profileSeed.getName() + "' seed");

                Actions.getProfiles(driver, account, profileSeed, profileRepository);

                profileSeed.setUsed(true);
                profileSeedRepository.save(profileSeed);

                profiles = profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false);

                if (profiles != null && profiles.size() > 4000) {
                    break;
                }
            }

            logger.info(account.getUsername() + " finished getting profiles from profile seeds. Current profile count is " + profiles.size());
        }
    }
}
