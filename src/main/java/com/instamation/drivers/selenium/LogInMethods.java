package com.instamation.drivers.selenium;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Action;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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

            String body = driver.getDriver().findElement(By.tagName("body")).getText();
            if(body.contains("Email") && body.contains("Phone")){
                String email = null;
                String phone = null;
                for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                    if(element.getText().contains("Email")){
                        email = element.getText();
                    }
                    if(element.getText().contains("Phone")){
                        phone = element.getText();
                    }
                }

                return "unusual-attempt-phone-and-email" + "|" + email + "|" + phone;
            }

            for (WebElement element : driver.getDriver().findElements(By.tagName("label"))) {
                if(element.getText().contains("Email")){
                    return "unusual-attempt-email";
                }
                if(element.getText().contains("Phone")){
                    return "unusual-attempt-phone";
                }
            }
        }catch (Exception e){}
        return "unusual-attempt";
    }
}
