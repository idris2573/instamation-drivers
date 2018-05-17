package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.*;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:8081", "https://insta-mation.com"})
@RequestMapping(value = "/account")
public class AccountController {

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
        // check if account exists and if the account exists and account does not belong to this user
        User user = userRepository.findById(userId).get();
        Account existingAccount = accountRepository.findByUsername(account.getUsername());
        if(existingAccount != null){
            if(!existingAccount.getUser().getId().equals(user.getId())){
                return new Response("exists");
            }
        }

        Driver driver = DriverList.get(accountRepository.findByUsername(account.getUsername()));
        Proxy proxy = proxyRepository.findFirstByAccount(null);

        if(driver != null && driver.isClosed()){
            driver = null;
        }
        if(driver == null) {
            if (proxy != null) {
                driver = new Driver(false, proxy.getIp());
            } else {
                driver = new Driver();
            }
        }

        String response = Actions.loginFirstTime(driver, account);

        // user enters the wrong credentials
        if(response.equalsIgnoreCase("wrong-credentials")){
            driver.close();
            try{
                DriverList.remove(account);
            }catch (Exception e){}
            return new Response("wrong-credentials");
        }


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


        if(response.contains("unusual-attempt")){
            account.setUser(user);
            account.setEnabled(false);

            // save proxy with account
            try {
                if(proxy != null) {
                    proxy.setAccount(account);
                    account.setProxy(proxy);
                    accountRepository.save(account);
                    proxyRepository.save(account.getProxy());
                }else {
                    accountRepository.save(account);
                }
            }catch (Exception e){}
            if(!DriverList.containsKey(account)) {
                DriverList.put(accountRepository.findByUsername(account.getUsername()), driver);
            }
            return new Response(response);
        }

        // successful login
        loginSuccess(driver, account);

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


        // user is confirmed via code. add user
        loginSuccess(driver, account);

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

        accountRepository.save(account);

        // create settings for the account and vice versa, setting has to be saved to the database first.
        Setting setting = new Setting();
        setting.setAccount(account);
        setting.setActionSpeed("normal");
        setting.updateSettingsSpeed();
        settingRepository.save(setting);
        account.setSetting(setting);

        // set expiry date for the first days free trial
        long day = 86400000;
        account.setExpiryDate(new Date(System.currentTimeMillis() + day*3));
        account.setEnabled(true);
        accountRepository.save(account);

        Stats stats = new Stats();
        stats.setAccount(account);
        stats.setFollowers(account.getFollowers());
        stats.setActions(account.getActions());
        stats.setFollowing(account.getFollowing());
        stats.setPostCount(account.getPostCount());
        statsRepository.save(stats);

    }


}
