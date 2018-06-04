package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Driver {
    private static final Logger logger = Logger.getLogger(Driver.class);

    private WebDriver driver;
    private Account account;
    private List<String> pids;

    public Driver(Account account) throws Exception {
        this.account = account;
        logger.info(account.getUsername() + " has started up a new driver");

        // get list of pids and save in array
        List<String> allChromePids = StaticMethods.checkChromeProcessPIDList();

        driver = driver(false);

        // compare list of pids and remove all but new pids
        List<String> pids = StaticMethods.checkChromeProcessPIDList();
        for(String pid : allChromePids){
            pids.remove(pid);
        }
        this.pids = pids;

        driver.manage().window().setPosition(new Point(-500,0));
        driver.manage().window().setSize(new Dimension(374,650));
    }

    public Driver(boolean headless, Account account) throws Exception {
        this.account = account;
        logger.info(account.getUsername() + " has started up a new driver");

        // get list of pids and save in array
        List<String> allChromePids = StaticMethods.checkChromeProcessPIDList();

        driver = driver(headless);

        // compare list of pids and remove all but new pids
        List<String> pids = StaticMethods.checkChromeProcessPIDList();
        for(String pid : allChromePids){
            pids.remove(pid);
        }
        this.pids = pids;

        driver.manage().window().setPosition(new Point(-500, 0));
        driver.manage().window().setSize(new Dimension(374, 650));
    }

    public Driver(boolean headless, String proxy, Account account) throws Exception {
        this.account = account;
        logger.info(account.getUsername() + " has started up a new driver");

        // get list of pids and save in array
        List<String> allChromePids = StaticMethods.checkChromeProcessPIDList();

        driver = driver(headless, proxy);

        // compare list of pids and remove all but new pids
        List<String> pids = StaticMethods.checkChromeProcessPIDList();
        for(String pid : allChromePids){
            pids.remove(pid);
        }
        this.pids = pids;

        driver.manage().window().setPosition(new Point(-500, 0));
        driver.manage().window().setSize(new Dimension(374, 650));
    }



    private WebDriver driver(boolean headless)  throws Exception{

        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Nexus 5");

        ChromeOptions chromeOptions = new ChromeOptions();
        if(headless) {
         chromeOptions.addArguments("--headless");
        }
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);

        String chromeDriver;
        File file;
        if(System.getProperty("os.name").equals("Linux")){
            chromeDriver = "/chromedriver-linux";
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--no-sandbox");
            file = new File("resources" + chromeDriver);
        } else {
            chromeDriver = "/chromedriver-windows2.exe";
            URL resource = Driver.class.getResource(chromeDriver);
            file = Paths.get(resource.toURI()).toFile();
        }

        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

        return new ChromeDriver(chromeOptions);
    }

    private WebDriver driver(boolean headless, String proxy)  throws Exception{

        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "Nexus 5");

        ChromeOptions chromeOptions = new ChromeOptions();
        if(headless) {
         chromeOptions.addArguments("--headless");
        }
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        chromeOptions.addArguments("--proxy-server=" + proxy);

        String chromeDriver;
        File file;
        if(System.getProperty("os.name").equals("Linux")){
            chromeDriver = "/chromedriver-linux";
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--no-sandbox");
            file = new File("resources" + chromeDriver);
        } else {
            chromeDriver = "/chromedriver-windows2.exe";
            URL resource = Driver.class.getResource(chromeDriver);
            file = Paths.get(resource.toURI()).toFile();
        }

        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

        return new ChromeDriver(chromeOptions);
    }


    public WebDriver getDriver() {
        return driver;
    }

    public void close(){
        if(driver == null || isClosed()){
            return;
        }

        logger.info(account.getUsername() + " driver has been closed");
        driver.close();
        driver.quit();

        if (pids == null || pids.isEmpty()) {
            return;
        }

        deletePids();
    }

    public boolean isClosed(){
        try{
            driver.getCurrentUrl();
            return false;
        }catch (Exception e){
            return true;
        }
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object obj) {
        Driver driver = (Driver) obj;
        if(driver.getAccount().equals(account)){
            return true;
        }
        return false;
    }

    public List<String> getPids() {
        return pids;
    }

    public void setPids(List<String> pids) {

        if(pids == null || pids.isEmpty()){
            return;
        }
        for(String pid: pids){
            logger.info(account.getUsername() + "'s adding new PID " + pid);
        }

        if (this.pids == null || this.pids.isEmpty()) {
            this.pids = pids;
            return;
        }

        deletePids();

        this.pids = pids;
    }

    private void deletePids(){
        for(String pid : pids){
            try{
                String cmd;
                if(System.getProperty("os.name").equals("Linux")) {
                    cmd = "kill " + pid;
                } else {
                    cmd = "taskkill /F /PID " + pid;
                }
                Runtime.getRuntime().exec(cmd);
//                logger.info(account.getUsername() + "'s driver killing PID " + pid);
            }catch (Exception e){}
        }

    }
}
