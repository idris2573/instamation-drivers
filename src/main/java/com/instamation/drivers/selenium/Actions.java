package com.instamation.drivers.selenium;

import com.instamation.drivers.model.*;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.repository.FollowerRepository;
import com.instamation.drivers.repository.PostRepository;
import com.instamation.drivers.repository.ProfileRepository;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;


public class Actions {

    private static final Logger logger = Logger.getLogger(Action.class);

    ////////////////////////////////////////////////ACCOUNT METHODS////////////////////////////////////////////////////////////

    public static String login(Driver driver, Account account) throws Exception{
        logger.info(account.getUsername() + " is attempting to log in");
        Thread.sleep(1000);

        // if already logged in
        if (LogInMethods.isLoggedIn(driver)) {
            logger.info(account.getUsername() + " is already logged in");
            return "success";
        }

        clickButton(driver, "Log In");
        Thread.sleep(1000);

        LogInMethods.inputLoginInfo(driver, account);

        clickButton(driver, "Log in");
        Thread.sleep(3000);

        // check if wrong credentials
        if(LogInMethods.isWrongCredentials(driver)){
            logger.info(account.getUsername() + " failed to log in with the wrong credentials");
            return "wrong-credentials";
        }

        if(LogInMethods.isUnusualAttempt(driver)){
            logger.info(account.getUsername() + " has an unusual attempt when logging in");
            return LogInMethods.getUnusualAttemptRecoveryType(driver);
        }

        // click something (cant remember)
        try{
            driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > button")).click();
        }catch (Exception e){}

        // add your phone number check
        clickButton(driver, "Close");

        clickNotNow(driver);
        isSaveLoginInfo(driver);
        logger.info(account.getUsername() + " has logged in successfully");

        return "success";
    }


    public static void updateProfileDetails(Driver driver, Account account, AccountRepository accountRepository) throws Exception{
        // select profile tab

        try{
            driver.getDriver().get("https://instagram.com/" + account.getUsername());

            Thread.sleep(600);

            String username = driver.getDriver().findElement(By.tagName("h1")).getText();
            //String bio = driver.getDriver().findElement(By.cssSelector("div._tb97a")).findElement(By.tagName("span")).getText().replaceAll("[^a-zA-Z0-9 ,-]","");
            String image = getImage(driver);

            int posts = getCount(driver, "posts");
            int followers = getCount(driver, "followers");
            int following = getCount(driver, "following");

            account.setUsername(username);
            //account.setBio(bio);
            account.setPostCount(posts);
            account.setFollowers(followers);
            account.setFollowing(following);
            account.setProfilePic(image);
            accountRepository.save(account);
            logger.info(account.getUsername() + " update profile details");
        }catch (Exception e){
            logger.info(account.getUsername() + " failed to update profile details");
            e.printStackTrace();
        }
    }

    public static void updateFollowers(Driver driver, Account account, FollowerRepository followerRepository){

        //click followers
        try {
            driver.getDriver().findElement(By.cssSelector("#react-root > section > main > article > ul > li:nth-child(2)")).click();
        }catch (Exception e){}
        try {
            driver.getDriver().findElement(By.cssSelector("a._t98z6._sf8d3")).click();
        }catch (Exception e){}

        //scroll through followers
        int i = 1;
        JavascriptExecutor js = ((JavascriptExecutor) driver.getDriver());
        while(i < 200){
            scroll(500*i, js);
            i++;
        }

        List<WebElement> elements = driver.getDriver().findElements(By.cssSelector("div._2nunc"));

        for(WebElement element : elements){
            if(followerRepository.findByAccountAndUsername(account, element.getText()) == null){
                Follower follower = new Follower();
                follower.setAccount(account);
                follower.setUsername(element.getText());
                followerRepository.save(follower);
            }
        }

    }

    public static boolean isNotAvailable(Driver driver){
        if(!driver.getDriver().findElements(By.tagName("h2")).isEmpty()) {
            if (driver.getDriver().findElement(By.tagName("h2")).getText().equalsIgnoreCase("Sorry, this page isn't available.")) {
                return true;
            }
        }

        if(!driver.getDriver().findElements(By.tagName("p")).isEmpty()) {
            if (driver.getDriver().findElement(By.tagName("p")).getText().equalsIgnoreCase("The link you followed may be broken, or the page may have been removed. Go back to Instagram.")) {
                return true;
            }
        }

        if(driver.getDriver().getTitle().contains("Page Not Found")){
            return true;
        }
        return false;

    }


    ////////////////////////////////////////////////PROFILE AUTOMATION METHODS////////////////////////////////////////////////////////////

    public static void followProfile(Driver driver, Profile profile){
//        driver.getDriver().get("https://instagram.com/" + profile.getUsername());

        clickButton(driver, "Follow");

    }

    public static void unfollowProfile(Driver driver, Profile profile){
//        driver.getDriver().get("https://instagram.com/" + profile.getUsername());
        clickButton(driver, "Following");
        clickButton(driver, "Requested");
    }

    public static void likeProfilePosts(Driver driver, Profile profile, Setting setting){

        List<String> postUrl = getPostUrls(driver);
        if(Actions.isPrivate(driver) || postUrl.isEmpty()){
            return;
        }
        int likesPerPage = new Random().nextInt(setting.getLikesPerPage()) + 1;

        for(int i = 0; i < likesPerPage; i++){
            driver.getDriver().get(postUrl.get(i));
            clickLike(driver);
        }

        driver.getDriver().get("https://instagram.com/" + profile.getUsername());

    }

    public static void commentProfilePosts(Driver driver, Profile profile, Comment comment){

        List<String> postUrl = getPostUrls(driver);

        int commentsPerPage = new Random().nextInt(2) + 1;

        for(int i = 0; i < commentsPerPage; i++){
            driver.getDriver().get(postUrl.get(i));
            clickComment(driver);
            try {
                driver.getDriver().findElement(By.tagName("textarea")).sendKeys(comment.decodeStringUrl());
                clickButton(driver, "Post");
            }catch (Exception e){}
        }

        driver.getDriver().get("https://instagram.com/" + profile.getUsername());

    }

    public static boolean isPrivate(Driver driver){
        try {
            String text = driver.getDriver().findElement(By.cssSelector("div._q8pf2._r1mv3")).getText();
            if (text.equalsIgnoreCase("This Account is Private\n" +
                    "Follow to see their photos and videos.")) {
                return true;
            }
        }catch (Exception e){}

        return false;
    }


    ////////////////////////////////////////////////SCRAPING METHODS////////////////////////////////////////////////////////////

    public static void getAutomatedPosts(Driver driver, String search, Account account, PostRepository postRepository){
        driver.getDriver().get("https://www.google.co.uk/imghp");

        driver.getDriver().findElement(By.name("q")).sendKeys(search);
        driver.getDriver().findElement(By.cssSelector("button.Tg7LZd")).click();

        List<WebElement> imageClickElements = driver.getDriver().findElements(By.cssSelector("div.rg_bx.rg_di.rg_el.ivg-i"));
        if(imageClickElements.isEmpty()){
            imageClickElements = driver.getDriver().findElements(By.cssSelector("div.bRMDJf"));
        }

        for(WebElement element : imageClickElements){
            boolean clicked = false;
            do {
                try {
                    element.click();
                    clicked = true;
                } catch (Exception e) {}
            }while (!clicked);

            String imageLink = driver.getDriver().findElements(By.className("irc_mi")).get(1).getAttribute("src");

            if(imageLink == null){
                imageLink = driver.getDriver().findElements(By.className("irc_mi")).get(0).getAttribute("src");
            }

            if(imageLink == null){
                imageLink = driver.getDriver().findElements(By.className("irc_mi")).get(2).getAttribute("src");
            }

            try {
                postRepository.save(new Post(account, imageLink));
            }catch (Exception e){}

            //click close
            clicked = false;
            do {
                try {
                    driver.getDriver().findElement(By.cssSelector("div.Hh7LVb")).click();
                    clicked = true;
                } catch (Exception e) {}
            }while (!clicked);
        }

    }

    public static void getProfiles(Driver driver, Account account, ProfileSeed profileSeed, ProfileRepository profileRepository) throws Exception{
        if(profileSeed.getType().equals("username")) {
            driver.getDriver().get("https://instagram.com/" + profileSeed.getName());
        } else {
            driver.getDriver().get("https://instagram.com/explore/tags/" + profileSeed.getName());
        }

        if(isNotAvailable(driver)){
            return;
        }

        Set<String> profileUsernames;
        List<Profile> profiles;

        JavascriptExecutor js = ((JavascriptExecutor) driver.getDriver());
        for(String postUrl : getPostUrls(driver)){
            driver.getDriver().get(postUrl);
            scroll(200, js);

            if(!clickLinkContains(driver, " like")){
                continue;
            }

            Thread.sleep(500);

            int i = 1;
            while(i < 1000){
                scroll(500*i, js);
                i++;
            }

            profileUsernames = getProfileUsernames(driver);
            if(profileUsernames == null || profileUsernames.isEmpty()){
                continue;
            }

            saveProfileUsernames(profileRepository, profileUsernames, profileSeed.getName(), account);

            profiles = profileRepository.findByAccount(account);
            if(profiles != null && profiles.size() > 4000){
                break;
            }

        }

    }

    ////////////////////////////////////////////////POSTING METHODS////////////////////////////////////////////////////////////

    public static void post(Driver driver, Post post, Account account, Caption caption, boolean login) throws Exception{

        if(login) {
            login(driver, account);
        } else {
            driver.getDriver().get("https://instagram.com/");
        }
        String hashtagString = "";

        if(account.getSetting().getHashtagCategory() != null && account.getSetting().getHashtagAmount() != 0){
            hashtagString = " | " + getRandomHashtagsAsString(getHashtags(driver, account.getSetting().getHashtagCategory().replace("#","")), account.getSetting().getHashtagAmount());
        }

        driver.getDriver().get("https://instagram.com/");

        JavascriptExecutor js = ((JavascriptExecutor) driver.getDriver());

        js.executeScript(
                "HTMLInputElement.prototype.click = function() {                     " +
                        "  if(this.type !== 'file') HTMLElement.prototype.click.call(this);  " +
                        "};                                                                  " );


        driver.getDriver().findElement(By.cssSelector("div._crp8c.coreSpriteFeedCreation")).click();

        String filepath = downloadImage(post);

        driver.getDriver().findElements(By.tagName("input")).get(3).sendKeys(filepath);

        int attempts = 0;
        do{
            try {
                Thread.sleep(500);
                clickButton(driver, "Next");
                break;
            } catch (Exception e) {
                attempts++;
            }
        } while(attempts >= 5);

        if(attempts >= 5){
            throw new Exception("Could not click the button") ;
        }

        //add caption
        Thread.sleep(500);
        driver.getDriver().findElement(By.tagName("textarea")).sendKeys("@" + account.getUsername() + " " +
                caption.getCaption() + " | Tag your friends" + hashtagString);

        clickButton(driver, "Share");

        new File(filepath).delete();

    }

    private static String downloadImage(Post post) throws IOException {

        File resourcesDirectory = new File("src/main/resources");
        String extention = post.getUrl().substring(post.getUrl().lastIndexOf("."));

        if(extention.contains("jpeg")){
            extention = ".jpeg";
        } else{
            extention = extention.substring(0, 4);
        }

        if(extention.contains("?")){
            extention = extention.substring(0, extention.indexOf("?")-1);
        }

        String filepath = resourcesDirectory.getAbsolutePath() + "/tempImages/" + post.getId() + extention;

        if(!new File(filepath).exists()) {
            try (InputStream in = new URL(post.getUrl()).openStream()) {
                Files.copy(in, Paths.get(filepath));
            }
        }

        return filepath;
    }

    public static List<String> getHashtags(Driver driver, String seedTag) throws Exception{
        List<String> hashtags = new ArrayList<>();

        driver.getDriver().get("https://www.instagram.com/explore/search/");

        driver.getDriver().findElement(By.cssSelector("#react-root > section > nav._f4a0g > div > header > div > h1 > div > input")).sendKeys("#" + seedTag);
        Thread.sleep(2000);

        // input initial seed hashtag
        String allInfo = driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > div > div > div")).getText();

        String[] allTagInfo = allInfo.split("#");

        for(String tag : allTagInfo){
            if(tag.length() > 1){
                tag = tag.substring(0, tag.indexOf("\n"));
                hashtags.add("#" + tag);
            }
        }
        return hashtags;
    }

    public static String getRandomHashtagsAsString(List<String> getHashtags, int amount){
        long seed = System.nanoTime();
        Collections.shuffle(getHashtags, new Random(seed));
        Queue<String> hashtagQueue = new ArrayDeque<>(getHashtags);

        String hashtagsAsString = "";

        int buffer = new Random().nextInt(4) + 1;
        amount = (amount - 3) + buffer;

        for(int i = 0; i < amount; i++){
            hashtagsAsString = hashtagsAsString + hashtagQueue.poll() + " ";
            if(hashtagQueue.isEmpty()) {
                break;
            }
        }

        return hashtagsAsString;
    }




    ////////////////////////////////////////////////PRIVATE METHODS////////////////////////////////////////////////////////////

    private static void clickNotNow(Driver driver) throws Exception{
        boolean clicked = false;
        int attempt = 0;
        while(!clicked && attempt <= 5){
            try{
                driver.getDriver().findElement(By.cssSelector("a._pzcwu._rqivq")).click();
                clicked = true;
            }catch (Exception e){}

            try{
                driver.getDriver().findElements(By.cssSelector("div._k0d2z")).get(4).getText();
                clicked = true;
            }catch (Exception e){}

            Thread.sleep(500);
            attempt++;
        }
    }

    private static void waitElement(Driver driver, WebElement element){
        boolean ready = false;
        do{
            try{
                element.getText();
                ready = true;
            }catch (Exception e){}

        }while(!ready);
    }


    public static void clickButton(Driver driver, String buttonText){
        for(int i = 0; i < driver.getDriver().findElements(By.tagName("button")).size(); i++) {
            if(driver.getDriver().findElements(By.tagName("button")).get(i).getText().equalsIgnoreCase(buttonText)){
                try {
                    driver.getDriver().findElements(By.tagName("button")).get(i).click();
                }catch (StaleElementReferenceException ex){
                    driver.getDriver().findElements(By.tagName("button")).get(i).click();
                }
                break;
            }
        }
    }

    public static void clickLink(Driver driver, String linkText){
        for(int i = 0; i < driver.getDriver().findElements(By.tagName("a")).size(); i++) {
            if(driver.getDriver().findElements(By.tagName("a")).get(i).getText().equalsIgnoreCase(linkText)){
                try {
                    driver.getDriver().findElements(By.tagName("a")).get(i).click();
                }catch (StaleElementReferenceException ex){
                    driver.getDriver().findElements(By.tagName("a")).get(i).click();
                }
                break;
            }
        }
    }

    public static boolean clickLinkContains(Driver driver, String linkText){
        try {
            for (int i = 0; i < driver.getDriver().findElements(By.tagName("a")).size(); i++) {
                if (driver.getDriver().findElements(By.tagName("a")).get(i).getText().contains(linkText)) {
                    try {
                        driver.getDriver().findElements(By.tagName("a")).get(i).click();
                    } catch (StaleElementReferenceException ex) {
                        driver.getDriver().findElements(By.tagName("a")).get(i).click();
                    }
                    return true;
                }
            }
        }catch (Exception e){}

        return false;
    }

    public static boolean doesButtonExist(Driver driver, String buttonText){
        try {
            for (int i = 0; i < driver.getDriver().findElements(By.tagName("button")).size(); i++) {
                try {
                    if (driver.getDriver().findElements(By.tagName("button")).get(i).getText().equalsIgnoreCase(buttonText)) {
                        return true;
                    }
                } catch (StaleElementReferenceException ex) {
                    if (driver.getDriver().findElements(By.tagName("button")).get(i).getText().equalsIgnoreCase(buttonText)) {
                        return true;
                    }
                }
            }
        }catch (Exception e){
            logger.error(driver.getAccount() + " doesButtonExist errored || buttonText: " + buttonText);
        }

        return false;
    }

    private static void clickContainsSignUp(Driver driver){
        List<WebElement> elements = driver.getDriver().findElements(By.tagName("button"));

        for(WebElement element : elements) {
            if(element.getText().contains("Sign up")){
                element.click();
                break;
            }
        }
    }

    private static void clickEqualsSignUp(Driver driver){
        List<WebElement> elements = driver.getDriver().findElements(By.tagName("button"));

        for(WebElement element : elements) {
            if(element.getText().equalsIgnoreCase("Sign up")){
                element.click();
                break;
            }
        }
    }

    private static void clickSkip(Driver driver){
        List<WebElement> elements = driver.getDriver().findElements(By.tagName("button"));

        for(WebElement element : elements) {
            if(element.getText().equalsIgnoreCase("Skip")){
                element.click();
                break;
            }
        }
    }

    private static String getImage(Driver driver){

        String image = "";
        if(!driver.getDriver().findElements(By.cssSelector("img._cuacn")).isEmpty()){
            image = driver.getDriver().findElement(By.cssSelector("img._cuacn")).getAttribute("src");
            return image;
        }

        if(!driver.getDriver().findElements(By.cssSelector("img._rewi8")).isEmpty()){
            image = driver.getDriver().findElement(By.cssSelector("img._rewi8")).getAttribute("src");
            return image;
        }

        if(!driver.getDriver().findElements(By.cssSelector("#react-root > section > main > div > header > div > div > div > button > img")).isEmpty()){
            image = driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > header > div > div > div > button > img")).getAttribute("src");
            return image;
        }

        if(!driver.getDriver().findElements(By.tagName("header")).isEmpty()){
            image = driver.getDriver().findElement(By.tagName("header")).findElement(By.tagName("img")).getAttribute("src");
            return image;
        }
        return null;
    }

    private static List<String> getPostUrls(Driver driver){
        List<WebElement> posts = driver.getDriver().findElements(By.tagName("a"));
        List<String> postUrls = new ArrayList<>();
        for(WebElement post : posts){
            String postUrl = post.getAttribute("href");
            if(postUrl.startsWith("https://www.instagram.com/p/")){
                postUrls.add(postUrl);
            }
        }
        return postUrls;
    }

    private static Set<String> getProfileUsernames(Driver driver){
        List<WebElement> profiles = new ArrayList<>();
        try {
            profiles = driver.getDriver().findElements(By.cssSelector("a._2g7d5.notranslate._o5iw8"));

            if (profiles.isEmpty()) {
                profiles = driver.getDriver().findElements(By.cssSelector("div.FsskP"));
            }

            Set<String> profileUsernames = new HashSet<>();
            for (WebElement profile : profiles) {
                profileUsernames.add(profile.getText());
            }
            return profileUsernames;
        }catch (Exception e){
            logger.error(driver.getAccount() + " errored at getProfileUsernames");
        }

        return null;
    }

    private static void saveProfileUsernames(ProfileRepository profileRepository, Set<String> profileUsernames, String parentProfile, Account account){
        Profile profile;
        for(String profileUsername : profileUsernames){
            try {
                if (!profileRepository.findByAccountAndUsername(account, profileUsername).isEmpty()) {
                    continue;
                }
            }catch (Exception e){}

            profile = new Profile();
            profile.setUsername(profileUsername);
            profile.setParentProfile(parentProfile);
            profile.setAccount(account);
            try {
                profileRepository.save(profile);
            }catch (Exception e){}
        }
    }

    private static void scroll(int scrollAmount, JavascriptExecutor js){
        try {
            js.executeScript("window.scrollTo(0, " + scrollAmount + ")");
        }catch (Exception e){}
    }

    private static void clickLike(Driver driver){
        try{
            driver.getDriver().findElement(By.cssSelector("span._8scx2.coreSpriteHeartOpen")).click();
        }catch (Exception e){}
    }

    private static void clickComment(Driver driver){
        try{
            driver.getDriver().findElement(By.cssSelector("span._8scx2.coreSpriteComment")).click();
        }catch (Exception e){}
    }


    private static boolean isSaveLoginInfo(Driver driver){
        if (driver.getDriver().findElement(By.tagName("body")).getText().contains("Save Your Login Info?")) {
            clickButton(driver, "Save Info");
            return true;
        }
        return false;
    }



    //    public static void login(Driver driver, Account account) throws Exception{
//        logger.info(account.getUsername() + " is attempting to log in");
//        driver.getDriver().get("https://www.instagram.com/accounts/login/");
//
//        Thread.sleep(1000);
//        clickLogin(driver);
//        Thread.sleep(1000);
//        driver.getDriver().findElement(By.name("username")).sendKeys(account.getUsername());
//        driver.getDriver().findElement(By.name("password")).sendKeys(account.getPassword());
//        clickButton(driver, "Log in");
//        Thread.sleep(1500);
//
//        driver.getDriver().findElement(By.tagName("body")).sendKeys(Keys.CONTROL + "n");
//        if(LogInMethods.isUnusualAttempt(driver)){
//            logger.info(account.getUsername() + " has an unusual attempt log in");
//        }
//
//        try{
//            driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > button")).click();
//        }catch (Exception e){}
//
//        // add your phone number check
//        clickButton(driver, "Close");
//
//        clickNotNow(driver);
//        System.out.println(6);
//        isSaveLoginInfo(driver);
//        System.out.println(7);
//        logger.info(account.getUsername() + " logged in");
//    }


    private static int getCount(Driver driver, String infoType){

        int type;
        switch(infoType){
            case "posts": type = 0;
                break;
            case "followers": type = 1;
                break;
            case "following": type = 2;
                break;
            default: type = 0;
        }
        String countString = null;
        if(!driver.getDriver().findElements(By.cssSelector("span._fd86t._he56w")).isEmpty()) {
            countString = driver.getDriver().findElements(By.cssSelector("span._fd86t._he56w")).get(type).getText().replace(",", "");
        }

        if(countString == null){
            try {
                switch (infoType) {
                    case "posts":
                        countString = driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > ul > li:nth-child(1) > span > span")).getText();
                        break;
                    case "followers":
                        countString = driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > ul > li:nth-child(2) > a > span")).getText();
                        break;
                    case "following":
                        countString = driver.getDriver().findElement(By.cssSelector("#react-root > section > main > div > ul > li:nth-child(3) > a > span")).getText();
                        break;
                    default:
                        ;
                }
            }catch (Exception e){
                return 0;
            }
        }

        if(countString.contains(".")){
            String point = countString.substring(countString.indexOf("."));
            point = point.substring(0, 2);
            countString = countString.replace(point, "");
        }
        countString = countString.replace("k", "000");
        countString = countString.replace("m", "000000");
        countString = countString.replace(",", "");

        return Integer.parseInt(countString);

    }

}
