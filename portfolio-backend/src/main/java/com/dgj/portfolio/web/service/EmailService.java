package com.dgj.portfolio.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendContactEmail(String from, String subject, String message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("daniegeorgejohn@gmail.com");  // your destination
        email.setSubject("Portfolio Website: " + subject);
        email.setText("From: " + from + "\n\n" + message);
        mailSender.send(email);
    }
}

