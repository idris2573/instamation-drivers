package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.User;
import com.instamation.drivers.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.sql.Date;

@Controller
@RequestMapping(value = "/pricing")
public class PricingController {

    @Autowired
    private AccountRepository accountRepository;

    @RequestMapping(value = {"/",""})
    public String pricing(Model model, Alert alert, Principal principal){

        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "pricing");
        model.addAttribute("updateUser", new User());
        model.addAttribute("addAccount", new Account());

        if(principal != null) {
            User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
            model.addAttribute("updateUser", user);
        }

        return "pricing";
    }

    @RequestMapping(value = "/success")
    public String success(Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request){

        String type = request.getParameter("type");
        Long id = Long.valueOf(request.getParameter("account_id"));

        Account account = accountRepository.findById(id).get();

        account.setEnabled(true);

        long day = 86400000;

        if(type.equalsIgnoreCase("MONTH")){
            account.setExpiryDate(new Date(System.currentTimeMillis() + (day*30)));
        } else {
            account.setExpiryDate(new Date(System.currentTimeMillis() + (day*7)));
        }

        // if account is not enabled, enable it.
        if(!account.isEnabled()){
            account.setEnabled(true);
            account.setPendingUpgrade(false);
        }else {

            // if account is enabled and account is not pending an upgrade.
            if(!account.isPendingUpgrade()){
                account.setPendingUpgrade(true);
            }
        }
        accountRepository.save(account);

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.SUCCESS,"You have successfully upgraded your account."));
        return "redirect:/account/" + account.getUsername();
    }
}
