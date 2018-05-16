package com.instamation.drivers.web.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        System.out.println(getErrorPath());
        return "redirect:/";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
