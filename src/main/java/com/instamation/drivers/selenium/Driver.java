package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Driver {
    private WebDriver driver;
    private Set<Cookie> cookies = new TreeSet<>();

    public Driver() throws Exception {
        driver = driver(false);
        driver.manage().window().setPosition(new Point(-500,0));
        driver.manage().window().setSize(new Dimension(374,650));
    }

    public Driver(boolean headless) throws Exception {
        driver = driver(headless);

        driver.manage().window().setPosition(new Point(-500, 0));
        driver.manage().window().setSize(new Dimension(374, 650));
    }

    public Driver(boolean headless, String proxy) throws Exception {
        driver = driver(headless, proxy);

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
            chromeDriver = "/chromedriver-windows.exe";
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
            chromeDriver = "/chromedriver-windows.exe";
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
        cookies = driver.manage().getCookies();
        driver.close();
        driver.quit();
    }

    public boolean isClosed(){
        try{
            driver.getCurrentUrl();
            return false;
        }catch (Exception e){
            return true;
        }
    }

    public static boolean containsAccount(Map<Account, Driver> drivers, Account account){

        for(Map.Entry entry : drivers.entrySet()){
            Account driverAccount = (Account) entry.getKey();
            if(driverAccount.equals(account)){
                return true;
            }
        }

        return false;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public Set<Cookie> getCookies() {
        return cookies;
    }

}
