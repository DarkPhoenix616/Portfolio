package com.dgj.portfolio.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendContactEmail(String from, String subject, String message, String name) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("daniegeorgejohn@gmail.com");  // your destination
        email.setSubject("Portfolio Website: " + subject);

        email.setText("From: " + from + "\n" +
                "Name: " + name + "\n\n" +
                "Message:\n" + message);

        email.setFrom(from); // <-- 'from' must be a valid email like "daniemahan@gmail.com"
        mailSender.send(email);
    }
}

