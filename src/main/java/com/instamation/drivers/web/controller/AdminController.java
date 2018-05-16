package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.User;
import com.instamation.drivers.selenium.DriverList;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

    @RequestMapping(value = {"/", ""})
    public String admin(Model model, Principal principal, Alert alert){

        User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

        if(!user.getUserType().getRole().equalsIgnoreCase("ROLE_ADMIN")){
            return "redirect:/";

        }

        model.addAttribute("drivers", DriverList.getDrivers());
        model.addAttribute("addAccount", new Account());
        model.addAttribute("alert", alert);
        model.addAttribute("updateUser", user);

        return "admin";
    }
}
