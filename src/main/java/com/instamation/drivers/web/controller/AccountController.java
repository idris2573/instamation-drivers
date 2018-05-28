package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.*;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.selenium.LogInMethods;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
@RequestMapping(value = "/account")
public class AccountController {

    private static final Logger logger = Logger.getLogger(AccountController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping(value = "/add/{userId}")
    public Response addAccount(@RequestBody Account account, @PathVariable Long userId) throws Exception{
        String password = account.getPassword();

        // check if account exists and if the account exists and account does not belong to this user
        User user = userRepository.findById(userId).get();
        Account existingAccount = accountRepository.findByUsername(account.getUsername());

        // if existing account exists and the id does not belong to you. Return already exists
        if(existingAccount != null ){
            if(!existingAccount.getUser().getId().equals(user.getId()) && !user.getUserType().getRole().equalsIgnoreCase("ROLE_ADMIN")){
                return new Response("exists");
            } else {
                account = existingAccount;
            }
        }

        Driver driver = DriverList.get(account);
        Proxy proxy;
        // if account has proxy, set proxy as accounts proxy. If not find a new proxy without an account.
        if(account.getProxy() != null){
            proxy = account.getProxy();
        }else {
            proxy = proxyRepository.findFirstByAccount(null);
        }

        // if driver is closed, set driver as null.
        if(driver != null && driver.isClosed()){
            driver = null;
        }

        // if a driver is null create a new driver.
        if(driver == null) {
            // if proxy exists, give the driver the proxy.
            if (proxy != null) {
                try {
                    driver = new Driver(false, proxy.getIp(), account);
                } catch (Exception e){
                    logger.info(account.getUsername() + " crashed creating a new driver with a proxy");
                    return new Response("login-fail");
                }
            } else {
                try {
                    driver = new Driver(account);
                }catch (Exception e){
                    logger.info(account.getUsername() + " crashed creating a new driver with NO proxy");
                    return new Response("login-fail");
                }
            }
            DriverList.getNewDrivers().add(driver);
        }

        account.setPassword(password);
        String response;

        try{
            response = Actions.login(driver, account);
        }catch (Exception e){
            driver.close();
            if(DriverList.getNewDrivers().contains(driver)){
                DriverList.getNewDrivers().remove(driver);
            }
            if(DriverList.containsKey(account)){
                DriverList.remove(account);
            }
            logger.info(account.getUsername() + " crashed and failed to login");
            return new Response("login-fail");
        }

        // user enters the wrong credentials
        if(response.equalsIgnoreCase("wrong-credentials")){
            // close drive and remove it from driver list if its already in the driver list.
            driver.close();
            if(DriverList.getNewDrivers().contains(driver)){
                DriverList.getNewDrivers().remove(driver);
            }
            if(DriverList.containsKey(account)){
                DriverList.remove(account);
            }
            return new Response("wrong-credentials");
        }

        // if account credentials are correct
        if(existingAccount == null){
            account.setUser(user);
        }else {
            account.setUser(existingAccount.getUser());
        }

        if(proxy != null) {
            proxy.setAccount(account);
            account.setProxy(proxy);
            accountRepository.save(account);
            proxyRepository.save(account.getProxy());
        }

        // save or update an account;
        // if account does not have an id, get the account from the database with the same username.
        accountRepository.save(account);
        if(account.getId() == null) {
            account = accountRepository.findByUsername(account.getUsername());
        }

        // if Driverlist does not contain account, add accoung and driver to Driverlist
        DriverList.put(accountRepository.findByUsername(account.getUsername()), driver);


        // skipped to entering security code
        try{
            Thread.sleep(1000);
            if(driver.getDriver().findElement(By.tagName("h2")).getText().equalsIgnoreCase("Enter Your Security Code")){

                String text = null;
                for (WebElement element : driver.getDriver().findElements(By.tagName("p"))) {
                    if(element.getText().startsWith("Enter the")){
                        text = element.getText();
                        break;
                    }
                }
                return new Response("security-code", text);
            }
        }catch (Exception e){}


        // if login runs into unusual attempt, return an unusual attempt response
        if(response.contains("unusual-attempt")){
            return new Response(response);
        }

        // check if actually logged in
        if(!LogInMethods.isLoggedIn(driver)) {
            logger.info(account.getUsername() + " is retrying logging in");
            Actions.login(driver, account);
        } else {
            logger.info(account.getUsername() + " confirmed is logged in");
        }

        account.setLoggedIn(true);
        account.setAvailable(true);
        accountRepository.save(account);

        // successful login
        loginSuccess(driver, account);

        DriverList.put(accountRepository.findByUsername(account.getUsername()), driver);

        return new Response("success");
    }

    @PostMapping(value = "/recovery")
    public Response recovery(HttpServletRequest request) throws Exception{
        String type = request.getParameter("type");
        String username = request.getParameter("username");

        Driver driver = DriverList.get(accountRepository.findByUsername(username));

        // click the recovery type
        if(type.equalsIgnoreCase("email")){
            for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                if(element.getText().contains("Email")){
                    element.click();
                }
            }
        } else {
            for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                if(element.getText().contains("Phone")){
                    element.click();
                }
            }
        }

        Actions.clickButton(driver, "Send Security Code");

        Thread.sleep(1000);
        String text = null;
        for (WebElement element : driver.getDriver().findElements(By.tagName("p"))) {
            if(element.getText().startsWith("Enter the")){
                text = element.getText();
                break;
            }
        }

        return new Response("success", text);
    }

    @PostMapping(value = "/verify")
    public Response verify(HttpServletRequest request) throws Exception{
        String code = request.getParameter("code");
        String username = request.getParameter("username");

        Account account = accountRepository.findByUsername(username);
        Driver driver = DriverList.get(account);

        driver.getDriver().findElement(By.id("security_code")).clear();
        driver.getDriver().findElement(By.id("security_code")).sendKeys(code);

        Actions.clickButton(driver, "Submit");

        Thread.sleep(500);
        try {
            if (driver.getDriver().findElement(By.id("form_error")).getText().contains("Please check the code")) {
                return new Response("wrong-code");
            }
        }catch (Exception e){}

        // check if actually logged in
        if(!LogInMethods.isLoggedIn(driver)) {
            Actions.login(driver, account);
        }

        account.setLoggedIn(true);
        account.setAvailable(true);
        accountRepository.save(account);

        // user is confirmed via code. add user
        loginSuccess(driver, account);

        DriverList.put(accountRepository.findByUsername(account.getUsername()), driver);

        return new Response("success");
    }

    @PostMapping(value = "/verify-new")
    public Response verifyNew(HttpServletRequest request){
        String username = request.getParameter("username");

        Account account = accountRepository.findByUsername(username);
        Driver driver = DriverList.get(account);

        Actions.clickLink(driver, "Get a new one");

        return new Response("success");
    }

    private void loginSuccess(Driver driver, Account account) throws Exception{
        // update profile details
        Actions.updateProfileDetails(driver, account);

        // like instamation
        driver.getDriver().get("https://www.instagram.com/instamation8/");
        Actions.clickButton(driver, "Follow");

        // create settings for the account and vice versa, setting has to be saved to the database first.
        if(account.getSetting() == null) {
            Setting setting = new Setting();
            setting.setAccount(account);
            setting.setActionSpeed("normal");
            setting.updateSettingsSpeed();
            settingRepository.save(setting);
            account.setSetting(setting);
        }

        // set expiry date for the first days free trial
        if(account.getExpiryDate() == null) {
            long day = 86400000;
            account.setExpiryDate(new Date(System.currentTimeMillis() + day * 3));
            account.setEnabled(true);
            accountRepository.save(account);
        }

        Stats stats = new Stats();
        stats.setAccount(account);
        stats.setFollowers(account.getFollowers());
        stats.setActions(account.getActions());
        stats.setFollowing(account.getFollowing());
        stats.setPostCount(account.getPostCount());
        statsRepository.save(stats);

    }


}
