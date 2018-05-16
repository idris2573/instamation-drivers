package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;


@Controller
@RequestMapping(value = "/login")
public class LoginController {

    @RequestMapping(value = {"/",""}, method = RequestMethod.GET)
    public String login(Model model, Principal principal, Alert alert){

        if(principal != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        model.addAttribute("alert", alert);

        return "login";
    }

}
