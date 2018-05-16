package com.instamation.drivers.web.controller;
import com.instamation.drivers.authenitcation.UserService;
import com.instamation.drivers.email.EmailService;
import com.instamation.drivers.function.PasswordGenerator;
import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.User;
import com.instamation.drivers.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
public class SignUpController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(path = "/signup")
    public String registerForm(Model model, Alert alert, Principal principal) {

        if(principal != null){
            return "redirect:/dashboard";
        }

        model.addAttribute("newUser", new User());
        model.addAttribute("alert", alert);
        return "signup";

    }

    @PostMapping("/signup/success")
    public String success(@ModelAttribute User user, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (userService.findByUsername(user.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,"Email already registered"));
            return "redirect:/signup";
        }

        if(user.getPassword().length() < 6){
            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,"Password has to be 6 or more characters"));
            return "redirect:/signup";
        }

        //all goes well
        user.setUserType(userTypeRepository.findByName("User"));

        userService.createUser(user);

        String url = request.getRequestURL().toString();
        url = url.substring(0, url.indexOf("/",10)) + "/";

        // Send email
        emailService.sendEmail(user, "Idris from Instamation", "Thanks for Signing up", welcomeEmail(), "unsubscribe?user=" + user.getUsername());

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.SUCCESS,"You have successfully been registered. Please now login with your email and password"));
        return "redirect:/login";

    }

    @RequestMapping("/forgot-password")
    public String forgotPassword(Model model, Alert alert){

        model.addAttribute("user", new User());
        model.addAttribute("alert", alert);

        return "forgotpass";
    }

    @PostMapping(path = "/recovery-success")
    public String recoverySuccess(@ModelAttribute User user, RedirectAttributes redirectAttributes, HttpServletRequest request){

        User registeredUser = userService.findByUsername(user.getUsername());

        if (registeredUser == null) {
            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"User does not exist"));
            return "redirect:/forgot-password";
        }else {

            PasswordGenerator passwordGenerator = new PasswordGenerator(15);

            String password = passwordGenerator.nextString();

            registeredUser.setPassword(password);

            userService.updateUser(registeredUser);

            String url = request.getRequestURL().toString();
            url = url.substring(0, url.indexOf("/",10)) + "/";

            String message = "Your new password is: <b>" + password + "</b><br/>Login here with your new temporary password: <a href=\"" + url + "login\">" + url + "login</a><br/>Reset your password from the user settings menu immediately after you log in.";

            emailService.sendEmail(user, "Reset your password", "Reset your password", message, url + "unsubscribe?user=" + user.getUsername());

            redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"Check your email to reset your password"));

            return "redirect:/login";
        }
    }

    @RequestMapping("/unsubscribe")
    public String unsubscribe(HttpServletRequest request, RedirectAttributes redirectAttributes){

        String username = request.getParameter("user");
        User user = userService.findByUsername(username);
        user.setSubscribed(false);
        userService.updateUser(user);

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.WARNING,username + " has been unsubscribed"));
        return "redirect:/";
    }

    private String welcomeEmail(){

        String email =
                "<p>Thanks for signing up with Instamation. We're excited to help you grow your audience!</p>" +
                "<p>If you haven't already, <a href=\"https://insta-mation.com/login\">connect your account</a> to get started.</p>" +
                "<p>Questions? Send us a message via our <a href=\"https://insta-mation.com/contact\">contact page</p> and we'll get back to you right away!</p>";
        return email;
    }

}
















