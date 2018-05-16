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

import java.security.Principal;
import java.util.List;

@Controller
public class PageController {

    @Autowired
    private AccountRepository accountRepository;

    @RequestMapping(value = {"/", ""})
    public String home(Model model, Alert alert, Principal principal){

        if(principal != null){
            return "redirect:/dashboard";
        }

        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "home");

        return "index";
    }

    @RequestMapping(value = "/dashboard")
    private String dashboard(Model model,  Alert alert, Principal principal){
        User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

        List<Account> accounts = null;
        if(!user.getUserType().getRole().equalsIgnoreCase("ROLE_ADMIN")){
            accounts = accountRepository.findByUser(user);
        } else {
            accounts = accountRepository.findAll();
        }

        model.addAttribute("addAccount", new Account());
        model.addAttribute("accounts", accounts);
        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "home");
        model.addAttribute("updateUser", user);
        model.addAttribute("totalFollowers", getTotalFollowers(accounts));

        return "dashboard";
    }

    private int getTotalFollowers(List<Account> accounts){
        int followers = 0;

        if(!accounts.isEmpty()) {
            for (Account account : accounts) {
                followers = followers + account.getFollowers();
            }
        }
        return followers;
    }

    @RequestMapping(value = "/faq")
    public String faq(Model model,  Alert alert, Principal principal){

        model.addAttribute("addAccount", new Account());
        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "faq");

        if(principal != null) {
            User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
            model.addAttribute("updateUser", user);
        }

        return "faq";
    }

    @RequestMapping(value = "/privacy")
    public String privacy(Model model,  Alert alert, Principal principal){

        model.addAttribute("addAccount", new Account());
        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "privacy");
        model.addAttribute("privacyText", privacyText());
        model.addAttribute("updateUser", new User());

        if(principal != null) {
            User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
            model.addAttribute("updateUser", user);
        }


        return "privacy";
    }

    @RequestMapping(value = "/terms")
    public String terms(Model model,  Alert alert, Principal principal){

        model.addAttribute("addAccount", new Account());
        model.addAttribute("alert", alert);
        model.addAttribute("activeNav", "terms");
        model.addAttribute("termsText", termsText());
        model.addAttribute("updateUser", new User());

        if(principal != null) {
            User user = ((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());
            model.addAttribute("updateUser", user);
        }

        return "terms";
    }



    private String privacyText(){
        return "<br/><style>\n" +
                "#ppBody\n" +
                "{\n" +
                "    font-size:11pt;\n" +
                "    width:100%;\n" +
                "    margin:0 auto;\n" +
                "    text-align:justify;\n" +
                "}\n" +
                "\n" +
                "#ppHeader\n" +
                "{\n" +
                "    font-family:verdana;\n" +
                "    font-size:21pt;\n" +
                "    width:100%;\n" +
                "    margin:0 auto;\n" +
                "}\n" +
                "\n" +
                ".ppConsistencies\n" +
                "{\n" +
                "    display:none;\n" +
                "}\n" +
                "</style><div id='ppBody'><div class='ppConsistencies'><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">Information Collection</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">Information Usage</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">Information Protection</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">Cookie Usage</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">3rd Party Disclosure</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links text-center\">3rd Party Links</div>\n" +
                "        </div><div class='col-2'></div></div><div style='clear:both;height:10px;'></div><div class='ppConsistencies'><div class='col-2'>\n" +
                "            <div class=\"col-12 quick-links2 gen-text-center\">Google AdSense</div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"col-12 quick-links2 gen-text-center\">\n" +
                "                    Fair Information Practices\n" +
                "                    <div class=\"col-8 gen-text-left gen-xs-text-center\" style=\"font-size:12px;position:relative;left:20px;\">Fair information<br> Practices</div>\n" +
                "                </div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"col-12 quick-links2 gen-text-center coppa-pad\">\n" +
                "                    COPPA\n" +
                "\n" +
                "                </div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"col-12 quick-links2 quick4 gen-text-center caloppa-pad\">\n" +
                "                    CalOPPA\n" +
                "\n" +
                "                </div>\n" +
                "        </div><div class='col-2'>\n" +
                "            <div class=\"quick-links2 gen-text-center\">Our Contact Information<br></div>\n" +
                "        </div></div><div style='clear:both;height:10px;'></div>\n" +
                "<div class='innerText'>This privacy policy has been compiled to better serve those who are concerned with how their 'Personally Identifiable Information' (PII) is being used online. PII, as described in US privacy law and information security, is information that can be used on its own or with other information to identify, contact, or locate a single person, or to identify an individual in context. Please read our privacy policy carefully to get a clear understanding of how we collect, use, protect or otherwise handle your Personally Identifiable Information in accordance with our website.<br></div><span id='infoCo'></span><br><div class='grayText'><strong>What personal information do we collect from the people that visit our blog, website or app?</strong></div><br /><div class='innerText'>When ordering or registering on our site, as appropriate, you may be asked to enter your name, email address, mailing address, phone number, credit card information  or other details to help you with your experience.</div><br><div class='grayText'><strong>When do we collect information?</strong></div><br /><div class='innerText'>We collect information from you when you register on our site, subscribe to a newsletter, fill out a form, Open a Support Ticket or enter information on our site.</div><br> <span id='infoUs'></span><br><div class='grayText'><strong>How do we use your information? </strong></div><br /><div class='innerText'> We may use the information we collect from you when you register, make a purchase, sign up for our newsletter, respond to a survey or marketing communication, surf the website, or use certain other site features in the following ways:<br><br></div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To personalize your experience and to allow us to deliver the type of content and product offerings in which you are most interested.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To improve our website in order to better serve you.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To allow us to better service you in responding to your customer service requests.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To administer a contest, promotion, survey or other site feature.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To quickly process your transactions.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To send periodic emails regarding your order or other products and services.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> To follow up with them after correspondence (live chat, email or phone inquiries)</div><span id='infoPro'></span><br><div class='grayText'><strong>How do we protect your information?</strong></div><br /><div class='innerText'>We do not use vulnerability scanning and/or scanning to PCI standards.</div><div class='innerText'>We do collect credit card information, but did not know PCI compliant scans are now required.</div><div class='innerText'>We do not use Malware Scanning.<br><br></div><div class='innerText'>Your personal information is contained behind secured networks and is only accessible by a limited number of persons who have special access rights to such systems, and are required to keep the information confidential. In addition, all sensitive/credit information you supply is encrypted via Secure Socket Layer (SSL) technology. </div><br><div class='innerText'>We implement a variety of security measures when a user places an order enters, submits, or accesses their information to maintain the safety of your personal information.</div><br><div class='innerText'>All transactions are processed through a gateway provider and are not stored or processed on our servers.</div><span id='coUs'></span><br><div class='grayText'><strong>Do we use 'cookies'?</strong></div><br /><div class='innerText'>We do not use cookies for tracking purposes </div><div class='innerText'><br>You can choose to have your computer warn you each time a cookie is being sent, or you can choose to turn off all cookies. You do this through your browser affiliate. Since browser is a little different, look at your browser's Help Menu to learn the correct way to modify your cookies.<br></div><br><div class='innerText'>If you turn cookies off, Some of the features that make your site experience more efficient may not function properly.that make your site experience more efficient and may not function properly.</div><br><span id='trDi'></span><br><div class='grayText'><strong>Third-party disclosure</strong></div><br /><div class='innerText'><strong>Do we disclose the information we collect to Third-Parties?</strong></div><div class='innerText'>We sell,trade, or otherwise transfer to outside parties your name, address,city,town, any form or online contact identifier email, name of chat account etc., screen name or user names, phone number Personally Identifiable Information.</div><br><span id='trLi'></span><br><div class='grayText'><strong>Third-party links</strong></div><br /><div class='innerText'>Occasionally, at our discretion, we may include or offer third-party products or services on our website. These third-party sites have separate and independent privacy policies. We therefore have no responsibility or liability for the content and activities of these linked sites. Nonetheless, we seek to protect the integrity of our site and welcome any feedback about these sites.</div><span id='gooAd'></span><br><div class='blueText'><strong>Google</strong></div><br /><div class='innerText'>Google's advertising requirements can be summed up by Google's Advertising Principles. They are put in place to provide a positive experience for users. https://support.google.com/adwordspolicy/answer/1316548?hl=en <br><br></div><div class='innerText'>We use Google AdSense Advertising on our website.</div><div class='innerText'><br>Google, as a third-party vendor, uses cookies to serve ads on our site. Google's use of the DART cookie enables it to serve ads to our users based on previous visits to our site and other sites on the Internet. Users may opt-out of the use of the DART cookie by visiting the Google Ad and Content Network privacy policy.<br></div><div class='innerText'><br><strong>We have implemented the following:</strong></div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Remarketing with Google AdSense</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Google Display Network Impression Reporting</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Demographics and Interests Reporting</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> DoubleClick Platform Integration<br></div><br><div class='innerText'>We, along with third-party vendors such as Google use first-party cookies (such as the Google Analytics cookies) and third-party cookies (such as the DoubleClick cookie) or other third-party identifiers together to compile data regarding user interactions with ad impressions and other ad service functions as they relate to our website. </div><div class='innerText'><br><strong>Opting out:</strong><br>\n" +
                "\t\t\t\t\tUsers can set preferences for how Google advertises to you using the Google Ad Settings page. Alternatively, you can opt out by visiting the Network Advertising Initiative Opt Out page or by using the Google Analytics Opt Out Browser add on.</div><span id='calOppa'></span><br><div class='blueText'><strong>California Online Privacy Protection Act</strong></div><br /><div class='innerText'>CalOPPA is the first state law in the nation to require commercial websites and online services to post a privacy policy.  The law's reach stretches well beyond California to require any person or company in the United States (and conceivably the world) that operates websites collecting Personally Identifiable Information from California consumers to post a conspicuous privacy policy on its website stating exactly the information being collected and those individuals or companies with whom it is being shared. -  See more at: http://consumercal.org/california-online-privacy-protection-act-caloppa/#sthash.0FdRbT51.dpuf<br></div><div class='innerText'><br><strong>According to CalOPPA, we agree to the following:</strong><br></div><div class='innerText'>Users can visit our site anonymously.</div><div class='innerText'>Once this privacy policy is created, we will add a link to it on our home page or as a minimum, on the first significant page after entering our website.<br></div><div class='innerText'>Our Privacy Policy link includes the word 'Privacy' and can easily be found on the page specified above.</div><div class='innerText'><br>You will be notified of any Privacy Policy changes:</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> On our Privacy Policy Page<br></div><div class='innerText'>Can change your personal information:</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> By emailing us</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> By logging in to your account</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> By chatting with us or by sending us a support ticket</div><div class='innerText'><br><strong>How does our site handle Do Not Track signals?</strong><br></div><div class='innerText'>We honor Do Not Track signals and Do Not Track, plant cookies, or use advertising when a Do Not Track (DNT) browser mechanism is in place. </div><div class='innerText'><br><strong>Does our site allow third-party behavioral tracking?</strong><br></div><div class='innerText'>It's also important to note that we allow third-party behavioral tracking</div><span id='coppAct'></span><br><div class='blueText'><strong>COPPA (Children Online Privacy Protection Act)</strong></div><br /><div class='innerText'>When it comes to the collection of personal information from children under the age of 13 years old, the Children's Online Privacy Protection Act (COPPA) puts parents in control.  The Federal Trade Commission, United States' consumer protection agency, enforces the COPPA Rule, which spells out what operators of websites and online services must do to protect children's privacy and safety online.<br><br></div><div class='innerText'>We do not specifically market to children under the age of 13 years old.</div><div class='innerText'>Do we let third-parties, including ad networks or plug-ins collect PII from children under 13?</div><span id='ftcFip'></span><br><div class='blueText'><strong>Fair Information Practices</strong></div><br /><div class='innerText'>The Fair Information Practices Principles form the backbone of privacy law in the United States and the concepts they include have played a significant role in the development of data protection laws around the globe. Understanding the Fair Information Practice Principles and how they should be implemented is critical to comply with the various privacy laws that protect personal information.<br><br></div><div class='innerText'><strong>In order to be in line with Fair Information Practices we will take the following responsive action, should a data breach occur:</strong></div><div class='innerText'>We will notify you via email</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Within 7 business days</div><div class='innerText'>We will notify the users via in-site notification</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Within 7 business days</div><div class='innerText'><br>We also agree to the Individual Redress Principle which requires that individuals have the right to legally pursue enforceable rights against data collectors and processors who fail to adhere to the law. This principle requires not only that individuals have enforceable rights against data users, but also that individuals have recourse to courts or government agencies to investigate and/or prosecute non-compliance by data processors.</div><span id='canSpam'></span><br><div class='blueText'><strong>CAN SPAM Act</strong></div><br /><div class='innerText'>The CAN-SPAM Act is a law that sets the rules for commercial email, establishes requirements for commercial messages, gives recipients the right to have emails stopped from being sent to them, and spells out tough penalties for violations.<br><br></div><div class='innerText'><strong>We collect your email address in order to:</strong></div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Send information, respond to inquiries, and/or other requests or questions</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Process orders and to send information and updates pertaining to orders.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Send you additional information related to your product and/or service</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Market to our mailing list or continue to send emails to our clients after the original transaction has occurred.</div><div class='innerText'><br><strong>To be in accordance with CANSPAM, we agree to the following:</strong></div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Not use false or misleading subjects or email addresses.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Identify the message as an advertisement in some reasonable way.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Include the physical address of our business or site headquarters.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Monitor third-party email marketing services for compliance, if one is used.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Honor opt-out/unsubscribe requests quickly.</div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Allow users to unsubscribe by using the link at the bottom of each email.</div><div class='innerText'><strong><br>If at any time you would like to unsubscribe from receiving future emails, you can email us at</strong></div><div class='innerText'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <strong>&bull;</strong> Follow the instructions at the bottom of each email.</div> and we will promptly remove you from <strong>ALL</strong> correspondence.</div><br><span id='ourCon'></span><br><div class='blueText'><strong>Contacting Us</strong></div><br /><div class='innerText'>If there are any questions regarding this privacy policy, you may contact us using the information below.<br><br></div><div class='innerText'>Instamation</div><div class='innerText'>21 The Moorings</div>London, E16 3JN <div class='innerText'>United Kingdom</div><div class='innerText'>admin@insta-mation.com</div><div class='innerText'><br></div></div>";
    }

    private String termsText(){
        return "<br/><h4>1. Terms</h4>\n" +
                "<p>By accessing the website at <a href=\"https://insta-mation.com\">https://insta-mation.com</a>, you are agreeing to be bound by these terms of service, all applicable laws and regulations, and agree that you are responsible for compliance with any applicable local laws. If you do not agree with any of these terms, you are prohibited from using or accessing this site. The materials contained in this website are protected by applicable copyright and trademark law.</p>\n" +
                "<h4>2. Use License</h4>\n" +
                "<ol type=\"a\">\n" +
                "   <li>Permission is granted to temporarily download one copy of the materials (information or software) on Instamation's website for personal, non-commercial transitory viewing only. This is the grant of a license, not a transfer of title, and under this license you may not:\n" +
                "   <ol type=\"i\">\n" +
                "       <li>modify or copy the materials;</li>\n" +
                "       <li>use the materials for any commercial purpose, or for any public display (commercial or non-commercial);</li>\n" +
                "       <li>attempt to decompile or reverse engineer any software contained on Instamation's website;</li>\n" +
                "       <li>remove any copyright or other proprietary notations from the materials; or</li>\n" +
                "       <li>transfer the materials to another person or \"mirror\" the materials on any other server.</li>\n" +
                "   </ol>\n" +
                "    </li>\n" +
                "   <li>This license shall automatically terminate if you violate any of these restrictions and may be terminated by Instamation at any time. Upon terminating your viewing of these materials or upon the termination of this license, you must destroy any downloaded materials in your possession whether in electronic or printed format.</li>\n" +
                "</ol>\n" +
                "<h4>3. Disclaimer</h4>\n" +
                "<ol type=\"a\">\n" +
                "   <li>The materials on Instamation's website are provided on an 'as is' basis. Instamation makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties including, without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights.</li>\n" +
                "   <li>Further, Instamation does not warrant or make any representations concerning the accuracy, likely results, or reliability of the use of the materials on its website or otherwise relating to such materials or on any sites linked to this site.</li>\n" +
                "</ol>\n" +
                "<h4>4. Limitations</h4>\n" +
                "<p>In no event shall Instamation or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption) arising out of the use or inability to use the materials on Instamation's website, even if Instamation or a Instamation authorized representative has been notified orally or in writing of the possibility of such damage. Because some jurisdictions do not allow limitations on implied warranties, or limitations of liability for consequential or incidental damages, these limitations may not apply to you.</p>\n" +
                "<h4>5. Accuracy of materials</h4>\n" +
                "<p>The materials appearing on Instamation website could include technical, typographical, or photographic errors. Instamation does not warrant that any of the materials on its website are accurate, complete or current. Instamation may make changes to the materials contained on its website at any time without notice. However Instamation does not make any commitment to update the materials.</p>\n" +
                "<h4>6. Links</h4>\n" +
                "<p>Instamation has not reviewed all of the sites linked to its website and is not responsible for the contents of any such linked site. The inclusion of any link does not imply endorsement by Instamation of the site. Use of any such linked website is at the user's own risk.</p>\n" +
                "<h4>7. Modifications</h4>\n" +
                "<p>Instamation may revise these terms of service for its website at any time without notice. By using this website you are agreeing to be bound by the then current version of these terms of service.</p>\n" +
                "<h4>8. Governing Law</h4>\n" +
                "<p>These terms and conditions are governed by and construed in accordance with the laws of England and you irrevocably submit to the exclusive jurisdiction of the courts in that State or location.</p>\n";
    }

}
