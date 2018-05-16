package com.instamation.drivers.email;

import com.instamation.drivers.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender javaMailSender;

    @Autowired
    MailContentBuilder mailContentBuilder;

    @Autowired
    public EmailService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(User user, String subject, String title, String message, String unsub){

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("admin@insta-mation.com", "Instamation");
            messageHelper.setTo(user.getUsername());
            messageHelper.setSubject(subject);

            String content = mailContentBuilder.build(title, message, unsub);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }

    }

    public void sendEmail(String userEmail, String subject, String title, String message, String unsub){

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("admin@insta-mation.com", "Instamation");
            messageHelper.setTo(userEmail);
            messageHelper.setSubject(subject);

            String content = mailContentBuilder.build(title, message, unsub);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }

    }

    public void sendAdminEmail(String subject, String title, String message, String unsub){

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("admin@insta-mation.com", "Instamation");
            messageHelper.setTo("idris2573@gmail.com");
            messageHelper.setSubject(subject);

            String content = mailContentBuilder.build(title, message, unsub);
            messageHelper.setText(content, true);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }

    }


}
