package com.instamation.drivers.test;

import com.instamation.drivers.model.Account;
import com.instamation.drivers.model.Response;
import com.instamation.drivers.repository.AccountRepository;
import com.instamation.drivers.repository.ProxyRepository;
import com.instamation.drivers.selenium.Driver;
import com.instamation.drivers.selenium.DriverList;
import com.instamation.drivers.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.apache.http.protocol.HTTP.USER_AGENT;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProxyRepository proxyRepository;

    @RequestMapping({"/", ""})
    @ResponseBody
    public String test() throws Exception{

        Driver driver = new Driver(false, "104.194.151.112:3199");
        driver.getDriver().get("http://whatismyipaddress.com");

        return "test page.";
    }

//    @RequestMapping("/{id}")
//    @ResponseBody
//    public String testProxy(@PathVariable Long id) throws Exception{
//
//        Account account = accountRepository.findById(id).get();
//
//        Driver driver = new Driver(false, account.getProxy().getIp());
////        if(Actions.login(driver, account)){
////            return "true";
////        }
//        driver.getDriver().get("http://whatismyipaddress.com");
//
//        return "false";
//    }

    @RequestMapping(value="/add", method = RequestMethod.POST)
    @ResponseBody
    public String add(@RequestBody String json, HttpServletRequest request) {

//        return new ResponseEntity<GenericResponse>(successGenericResponse, HttpStatus.OK);
        return "";
    }

    @PostMapping(value = "/2")
    @ResponseBody
    public Response test2(@RequestBody Account account) {

        // Create Response Object
        Response response = new Response("Done");
        return response;
    }

    @RequestMapping(value = "/email")
    @ResponseBody
    public String email(){

        emailService.sendEmail("idris2573@gmail.com", "Test Message", "Thanks for Signing up", welcomeEmail(), "unsubscribe?user=" + "idris2573");

        return "test email";
    }

    private String welcomeEmail(){

        String email = "<h2>Welcome!</h2>" +
                "<p>Thanks for signing up with Instamation. We're excited to help you grow your audience!</p>" +
                "<p>If you haven't already, <a href=\"https://instamation.com/login\">connect your account</a> to get started.</p>" +
                "<p>Questions? Send us a message via our <a href=\"https://instamation.com/contact\">contact page</p> and we'll get back to you right away!</p>";
        return email;
    }

    @RequestMapping(value = "/request")
    @ResponseBody
    public String request() throws Exception {

        String url = "http://localhost:8081/faq";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();

        return url.toString();
    }

    @RequestMapping(value = "/equals")
    @ResponseBody
    public String accountEquals() throws Exception {

        Account account = accountRepository.findByUsername("maturecolors");
        Account account2 = accountRepository.findByUsername("maturecolors");
        Account account3 = accountRepository.findByUsername("maturecolors");

        Account account4 = account3;

        DriverList.getDrivers().put(account, new Driver());


        return "yes";
    }

    @RequestMapping(value = "/start")
    @ResponseBody
    public String start() throws Exception{
        DriverList.startDrivers(proxyRepository);

        return "ok";
    }

}
