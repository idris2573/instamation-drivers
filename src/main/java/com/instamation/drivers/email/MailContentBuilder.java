package com.instamation.drivers.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {

    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(String heading, String paragraph, String unsub) {
        Context context = new Context();
        context.setVariable("heading", heading);
        context.setVariable("paragraph", paragraph);
        context.setVariable("unsub", unsub);
        return templateEngine.process("emails/email-template", context);
    }

}
