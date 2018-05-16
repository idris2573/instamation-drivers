package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.*;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
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

@Controller
@RequestMapping(value = "/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileSeedRepository profileSeedRepository;

    @Autowired
    private CommentRepository commentRepository;

    @RequestMapping(value = "/{username}")
    public String account(@PathVariable String username, Model model, Alert alert, HttpServletRequest request, Principal principal){

        User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
        Account account = null;

        try{
            account = accountRepository.findByUsername(username);
        }catch (Exception e){
            return "redirect:/dashboard";
        }

        if(!user.getUserType().getRole().equalsIgnoreCase("ROLE_ADMIN")){
            if(account.getUser() == null || !account.getUser().equals(user)) {
                return "redirect:/dashboard";
            }
        }

        model.addAttribute("account", account);
        model.addAttribute("alert", alert);
        model.addAttribute("profile", new Profile());
        model.addAttribute("pendingFollowing", profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false).size());
        model.addAttribute("following", profileRepository.findByAccountAndFollowing(account, true).size());
        model.addAttribute("unfollowed", profileRepository.findByAccountAndUnfollowed(account, true).size());
        model.addAttribute("liked", profileRepository.findByAccountAndLiked(account, true).size());
        model.addAttribute("commented", profileRepository.findByAccountAndCommented(account, true).size());
        model.addAttribute("setting", account.getSetting());
        model.addAttribute("posts", postRepository.findByAccount(account));
        model.addAttribute("posted", postRepository.findByAccountAndPosted(account, true).size());
        model.addAttribute("addAccount", new Account());
        model.addAttribute("updateUser", user);
        model.addAttribute("latestFollowers", followerRepository.findFirst20ByAccountOrderByIdDesc(account));
        model.addAttribute("allFollowers", followerRepository.findByAccount(account));
        model.addAttribute("latestActions", actionRepository.findFirst20ByAccountOrderByDateDesc(account));

        model.addAttribute("usernames", profileSeedRepository.findByAccountAndType(account, "username"));
        model.addAttribute("tags", profileSeedRepository.findByAccountAndType(account, "tag"));
        model.addAttribute("comments", commentRepository.findByAccount(account));


        long day = 86400000;
        List<Stats> stats7 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - day * 7));
        List<Stats> stats28 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - (day * 28)));
        List<Stats> stats90 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - (day * 90)));
        addGraph(model, request, stats7, stats28, stats90);

        // calculate new followers
        try {
            List<Stats> yesterdayList = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - day * 1));

            int yesterdayFollowers = yesterdayList.get(0).getFollowers();
            int todayFollowers = yesterdayList.get(yesterdayList.size()-1).getFollowers();

            int newFollowersToday = todayFollowers - yesterdayFollowers;
            model.addAttribute("newFollowersToday", newFollowersToday);
        }catch (Exception e){
            model.addAttribute("newFollowersToday", 0);
        }

        String upgrade = request.getParameter("upgrade");
        if(!account.isEnabled() || (upgrade != null && upgrade.equals("true"))){
            return "account-upgrade";
        } else {
            return "account";
        }
    }

    @PostMapping(value = "/save-settings")
    @ResponseBody
    public Response saveSettings(@RequestBody Setting setting ){

        Account account = accountRepository.findById(setting.getId()).get();
        Setting saveSetting = account.getSetting();
        saveSetting.setUnfollow(setting.isUnfollow());
        saveSetting.setFollow(setting.isFollow());
        saveSetting.setComment(setting.isComment());
        saveSetting.setLikes(setting.isLikes());

        saveSetting.setActionSpeed(setting.getActionSpeed().toLowerCase());
        saveSetting.setMediaType(setting.getMediaType());
        saveSetting.setMinLikesFilter(setting.getMinLikesFilter());
        saveSetting.setMaxLikesFilter(setting.getMaxLikesFilter());

        saveSetting.updateSettingsSpeed();

//        saveSetting.setAutopost(setting.isAutopost());
//        saveSetting.setPostsPerDay(setting.getPostsPerDay());
//        saveSetting.setHashtagCategory(setting.getHashtagCategory());
//        saveSetting.setHashtagAmount(setting.getHashtagAmount());



        settingRepository.save(saveSetting);

        return new Response("success");
    }

    @PostMapping(value = "/seed/add")
    @ResponseBody
    public Response addProfileSeed(@RequestBody ProfileSeed profileSeed, HttpServletRequest request){

        String seedType = request.getParameter("type");
        String accountId = request.getParameter("account_id");

        Account account = accountRepository.findById(Long.valueOf(accountId)).get();
        profileSeed.setAccount(account);
        profileSeed.setType(seedType);

        if( profileSeedRepository.findByAccountAndTypeAndName(account, profileSeed.getType(), profileSeed.getName()).isEmpty()){
            profileSeedRepository.save(profileSeed);
        }

        return new Response("success");
    }

    @PostMapping(value = "/seed/delete")
    @ResponseBody
    public Response deleteSeed(HttpServletRequest request){
        String accountId = request.getParameter("account_id");

        Long id = Long.valueOf(request.getParameter("seed_id"));
        ProfileSeed profileSeed = profileSeedRepository.findById(id).get();
        profileSeedRepository.delete(profileSeed);

        return new Response("success");
    }

    @PostMapping(value = "/comment/add")
    @ResponseBody
    public Response addComment(@RequestBody Comment comment, HttpServletRequest request){

        String accountId = request.getParameter("account_id");
        Account account = accountRepository.findById(Long.valueOf(accountId)).get();
        comment.setAccount(account);

        if( commentRepository.findByAccountAndDescription(account, comment.getDescription()).isEmpty()){
            commentRepository.save(comment);
        }

        return new Response("success");
    }

    @PostMapping(value = "/comment/delete")
    @ResponseBody
    public Response deleteComment(HttpServletRequest request){
        String accountId = request.getParameter("account_id");
        Account account = accountRepository.findById(Long.valueOf(accountId)).get();

        Long id = Long.valueOf(request.getParameter("comment_id"));
        Comment comment = commentRepository.findById(id).get();
        commentRepository.delete(comment);

        return new Response("success");
    }


    @PostMapping(value = "/add/{userId}")
    @ResponseBody
    public Response addAccount(@RequestBody Account account, RedirectAttributes redirectAttributes, @PathVariable Long userId) throws Exception{

        Proxy proxy = proxyRepository.findFirstByAccount(null);

        Driver driver;
        if(account.getProxy() != null){
            driver = new Driver(false, account.getProxy().getIp());
        } else {
            driver = new Driver();
        }

        String response = Actions.loginFirstTime(driver, account);

        if(response.equalsIgnoreCase("wrong-credentials")){
            driver.close();
            return new Response("wrong-credentials");
        }

        if(response.contains("unusual-attempt")){
//            driver.close();
            return new Response(response);
        }

        Actions.updateProfileDetails(driver, account);

        // like instamation
        driver.getDriver().get("https://www.instagram.com/instamation8/");
        Actions.clickButton(driver, "Follow");

//        driver.close();

        account.setUser(userRepository.findById(userId).get());
        accountRepository.save(account);

        // add proxy if one is available.
        if(proxy != null) {
            proxy.setAccount(account);
            account.setProxy(proxy);
            proxyRepository.save(account.getProxy());
        }

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

        accountRepository.save(account);

        Stats stats = new Stats();
        stats.setAccount(account);
        stats.setFollowers(account.getFollowers());
        stats.setActions(account.getActions());
        stats.setFollowing(account.getFollowing());
        stats.setPostCount(account.getPostCount());
        statsRepository.save(stats);

        DriverList.put(account, driver);

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Your account has been added"));
        return new Response("success");
    }

    private void addGraph(Model model, HttpServletRequest request, List<Stats> stats7, List<Stats> stats28, List<Stats> stats90){
//        Graph graph = new Graph(stats);
        Graph2 graph = new Graph2(stats7, stats28, stats90);

        if(request.getParameter("d") == null){
            model.addAttribute("statsGraph", graph.getMap7());
            model.addAttribute("dates", "7 days");

        } else if(request.getParameter("d").equalsIgnoreCase("28")){
            model.addAttribute("statsGraph", graph.getMap28());
            model.addAttribute("dates", "28 days");

        } else if(request.getParameter("d").equalsIgnoreCase("90")){
            model.addAttribute("statsGraph", graph.getMap90());
            model.addAttribute("dates", "90 days");

        } else {
            model.addAttribute("statsGraph", graph.getMap7());
            model.addAttribute("dates", "7 days");

        }

    }


//    @PostMapping(value = "/get-profiles")
//    public String getProfiles(@ModelAttribute Profile profile, HttpServletRequest request, RedirectAttributes redirectAttributes) throws Exception{
//
//        String id = request.getHeader("referer");
//        id = id.substring(id.lastIndexOf("/")+1);
//
//        Account account = accountRepository.findById(Long.parseLong(id)).get();
//
//        Driver driver = new Driver();
//        Actions.getProfiles(driver, account, profile, profileRepository);
//        driver.close();
//
//        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Added users from " + profile.getUsername() + " page"));
//
//        return "redirect:/account/" + id;
//    }








    // Admin stuff
    @RequestMapping(value = "/{username}/old")
    public String accountOld(@PathVariable String username, Model model, Alert alert, HttpServletRequest request){

        Account account = accountRepository.findByUsername(username);

        model.addAttribute("account", account);
        model.addAttribute("alert", alert);
        model.addAttribute("profile", new Profile());
        model.addAttribute("pendingFollowing", profileRepository.findByAccountAndFollowingAndUnfollowed(account, false, false).size());
        model.addAttribute("following", profileRepository.findByAccountAndFollowing(account, true).size());
        model.addAttribute("unfollowed", profileRepository.findByAccountAndUnfollowed(account, true).size());
        model.addAttribute("liked", profileRepository.findByAccountAndLiked(account, true).size());
        model.addAttribute("commented", profileRepository.findByAccountAndCommented(account, true).size());
        model.addAttribute("setting", account.getSetting());
        model.addAttribute("posts", postRepository.findByAccount(account));
        model.addAttribute("posted", postRepository.findByAccountAndPosted(account, true).size());

        long day = 86400000;
        List<Stats> stats7 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - day * 7));
        List<Stats> stats28 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - (day * 28)));
        List<Stats> stats90 = statsRepository.findByAccountAndDateGreaterThanEqual(account, new Date(System.currentTimeMillis() - (day * 90)));
        addGraph(model, request, stats7, stats28, stats90);

        return "account-old";
    }

    @RequestMapping(value = "/update/{id}")
    public String updateAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) throws Exception{

        Account account = accountRepository.findById(id).get();

        Driver driver;
        if(DriverList.containsKey(account)){
            driver = DriverList.get(account);
        } else {
            driver = new Driver();
        }

        Actions.updateProfileDetails(driver, account);
        account.updateStats(statsRepository);
        accountRepository.save(account);
//        driver.close();

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Your account has been update"));

        return "redirect:/account/" + id;
    }

}
