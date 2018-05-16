package com.instamation.drivers.web.controller;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Alert;
import com.instamation.drivers.model.Ticket;
import com.instamation.drivers.model.User;
import com.instamation.drivers.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping(value = "/contact")
public class ContactController {

    @Autowired
    private TicketRepository ticketRepository;

    @RequestMapping({"/", ""})
    public String index(Model model, Alert alert, Principal principal){

        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "contact");
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("addAccount", new Account());
        model.addAttribute("updateUser", new User());

        if(principal != null) {
            User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
            model.addAttribute("updateUser", user);
        }

        return "contact";
    }

    @PostMapping("/send")
    public String sendTicket(@ModelAttribute Ticket ticket, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {

        ticketRepository.save(ticket);

        String url = request.getRequestURL().toString();
        url = url.substring(0, url.indexOf("/",10)) + "/";

        String message = ticket.toString().replace("\n","<br/>");
        //emailService.sendAdminEmail("Contact Form", "You have a message!", message, url + "unsubscribe?user=" + "idris2573@gmail.com");

        redirectAttributes.addFlashAttribute("alert", new Alert(Alert.Status.PRIMARY,"You have successfully sent a message. A member of our staff will message you soon."));

        if(request.getHeader("Referer").contains("contact")) {
            return "redirect:/contact";
        }else {
            return "redirect:/";
        }
    }

}
