package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Action;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class LogInMethods {

    private static final Logger logger = Logger.getLogger(LogInMethods.class);

    public static void inputLoginInfo(Driver driver, Account account){
        boolean loaded = false;
        do{
            try{
                driver.getDriver().findElement(By.name("username")).sendKeys(account.getUsername());
                driver.getDriver().findElement(By.name("password")).sendKeys(account.getPassword());
                loaded = true;
            }catch (Exception e){}
        }while (!loaded);
    }

    public static boolean isWrongCredentials(Driver driver){
        if(driver.getDriver().getCurrentUrl().equalsIgnoreCase("https://www.instagram.com/accounts/login/")){
            try{
                if(driver.getDriver().findElement(By.id("slfErrorAlert")).getText().contains("Sorry, your password was incorrect")){
                    return true;
                }
            }catch (Exception e){}

            try{
                if(driver.getDriver().findElement(By.id("slfErrorAlert")).getText().contains("The username you entered doesn't belong to an account")){
                    return true;
                }
            }catch (Exception e){}

            try{
                if(driver.getDriver().findElement(By.tagName("body")).getText().contains("Incorrect Password")){
                    return true;
                }
            }catch (Exception e){}
        }
        return false;
    }

    public static boolean isUnusualAttempt(Driver driver){
        if(driver.getDriver().findElement(By.tagName("body")).getText().contains("We Detected An Unusual Login Attempt")){
            return true;
        }
        return false;
    }

    public static String getUnusualAttemptRecoveryType(Driver driver){
        try {

            String email = null;
            String phone = null;
            String body = driver.getDriver().findElement(By.tagName("body")).getText();

            if(body.contains("Email") && body.contains("Phone")){

                for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                    if(element.getText().contains("Email")){
                        email = element.getText();
                    }
                    if(element.getText().contains("Phone")){
                        phone = element.getText();
                    }
                }

                return "unusual-attempt-phone-and-email|" + email + "|" + phone;
            }

            for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                if(element.getText().contains("Email")){
                    email = element.getText();
                    return "unusual-attempt-email|" + email;
                }
                if(element.getText().contains("Phone")){
                    phone = element.getText();
                    return "unusual-attempt-phone|" + phone;
                }
            }
        }catch (Exception e){}
        return "unusual-attempt";
    }

    public static boolean isLoggedIn(Driver driver){

        if(driver == null || driver.getDriver() == null || driver.isClosed()){
            logger.error("ERROR driver is not logged in because driver does not exist - " + driver.getAccount());
            return false;
        }

        try {
            driver.getDriver().get("https://www.instagram.com/accounts/login/");
        }catch (Exception e){
            return false;
        }

        try{
            Thread.sleep(4000);
        }catch (Exception e){}

        try {
            if (Actions.doesButtonExist(driver, "Log In")) {
                return false;
            }
        }catch (Exception e){
            return false;
        }

        if(Actions.doesButtonExist(driver, "Continue with Facebook")){
            return false;
        }

        if(!driver.getDriver().findElements(By.name("username")).isEmpty()){
            return false;
        }

        if(!driver.getDriver().findElements(By.name("password")).isEmpty()){
            return false;
        }

        return true;
    }
}
