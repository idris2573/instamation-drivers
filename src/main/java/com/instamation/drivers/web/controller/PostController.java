package com.instamation.drivers.web.controller;


import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Post;
import com.instamation.drivers.model.Setting;
import com.instamation.drivers.repository.*;
import com.instamation.drivers.selenium.Actions;
import com.instamation.drivers.selenium.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;

//@Controller
//@Component
//@RequestMapping(value = "/posts")
public class PostController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Autowired
    private CaptionRepository captionRepository;

    @Autowired
    private StatsRepository statsRepository;

    @PostMapping(value = "/scrape/{id}")
    public String scrape(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) throws Exception{

        Account account = accountRepository.findById(id).get();
        String search = request.getParameter("search");
        Driver driver = new Driver(account);
        Actions.getAutomatedPosts(driver, search, account, postRepository);
        driver.close();

//        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Added " + search + " posts"));

        return "redirect:/account/" + id;
    }

    @RequestMapping(value = "/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes){

        postRepository.delete(postRepository.findById(id).get());
//        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,"Post deleted."));

        String referer = request.getHeader("referer");
        return "redirect:" + referer;
    }

    @Scheduled(fixedRate = 30000L)
    @RequestMapping(value = "/autopost")
    public void post() throws Exception{

        List<Account> accounts = accountRepository.findByRunningAndEnabled(true, true);

        for(Account account : accounts) {
            Setting setting = account.getSetting();

            if(!setting.isWorkingTime() || setting.getPostActions() >= setting.getPostsPerDay() || !setting.isAutopost() || !setting.isPostTime(postRepository)){
                continue;
            }

            Post post = postRepository.findFirstByAccountAndPosted(account, false);

            if(post != null) {
                Driver driver = new Driver(account);
                try {
                    Actions.post(driver, post, account, captionRepository.findRandom(), true);

                    setting.setPostActions(setting.getPostActions() + 1);
                    settingRepository.save(setting);
                    post.setPostDate(new Timestamp(System.currentTimeMillis()));
                    post.setPosted(true);
                    postRepository.save(post);

                    Actions.updateProfileDetails(driver, account, accountRepository);
                    account.updateStats(statsRepository);
                    accountRepository.save(account);
                    driver.close();

                }catch (Exception e){
                    postRepository.delete(post);
                }
            }
        }

    }

    @RequestMapping(value = "/initializeposts/{id}")
    public String initializeposts(@PathVariable Long id, RedirectAttributes redirectAttributes) throws Exception{

        Account account = accountRepository.findById(id).get();
        Setting setting = account.getSetting();
        Driver driver = new Driver(account);
        Actions.login(driver, account);

        while (account.getPostCount() < 9) {
            Post post = postRepository.findFirstByAccountAndPosted(account, false);

            if (post.getUrl() == null){
                postRepository.delete(post);
                continue;
            }

            if (post != null) {
                try {
                    Actions.post(driver, post, account, captionRepository.findRandom(), false);
                    Actions.updateProfileDetails(driver, account, accountRepository);

                    setting.setPostActions(setting.getPostActions() + 1);
                    settingRepository.save(setting);
                    post.setPostDate(new Timestamp(System.currentTimeMillis()));
                    post.setPosted(true);
                    postRepository.save(post);
                }catch (Exception e){
                    postRepository.delete(post);
                }
            }
        }
        driver.close();

//        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Initialized posts"));

        return "redirect:/account/" + id;
    }

}
