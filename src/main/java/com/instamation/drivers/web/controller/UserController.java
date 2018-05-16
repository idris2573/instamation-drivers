package com.instamation.drivers.web.controller;

import com.instamation.drivers.authenitcation.UserService;
import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/update")
    private String update(@ModelAttribute User user, Principal principal, RedirectAttributes redirectAttributes, HttpServletRequest request){

        User currentUser = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

        if (!currentUser.getEmail().equalsIgnoreCase(user.getEmail()) && userService.findByEmail(user.getEmail()) != null) {
            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,"Email already registered"));
            String referer = request.getHeader("referer");
            return "redirect:" + referer;
        }

        if(user.getPassword() != null && user.getPassword().length() < 6){
            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,"Password has to be 6 or more characters"));
            String referer = request.getHeader("referer");
            return "redirect:" + referer;
        }


        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setEmail(user.getEmail());
        currentUser.setUsername(user.getEmail());

        if(user.getPassword() != null && !user.getPassword().isEmpty()) {
            currentUser.setPassword(user.getPassword());
        }

        userService.updateUser(currentUser);
        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.SUCCESS, "Your user account has been updated"));

        String referer = request.getHeader("referer");
        return "redirect:" + referer;
    }
}

