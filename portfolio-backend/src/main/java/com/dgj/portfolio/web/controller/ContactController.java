package com.dgj.portfolio.web.controller;

import com.dgj.portfolio.web.config.JwtTokenProvider;
import com.dgj.portfolio.web.model.entity.User;
import com.dgj.portfolio.web.service.EmailService;
import com.dgj.portfolio.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/contact")
public class ContactController {

    private final UserService userService;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    public ContactController(UserService userService, EmailService emailService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Map<String, String> body) {

        try {
            String email = jwtTokenProvider.getEmailFromToken(jwt);
            System.out.println("Got email: " + email);

            String subject = body.get("subject");
            String message = body.get("message");

            emailService.sendContactEmail(email, subject, message);
            return ResponseEntity.ok("Message sent!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Message not sent!");
        }
    }

}

