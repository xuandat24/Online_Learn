package com.morrow.learning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LoginEmailService {
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public LoginEmailService(JavaMailSender mailSender,
                             @Value("${app.mail.from:no-reply@morrow.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendLoginInformation(String fullName, String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Your Morrow learning account is ready");
        message.setText("Hello " + fullName + ",\n\n"
                + "Your course registration has been paid. Sign in at the Morrow learning portal with:\n"
                + "Email: " + email + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "Please change this password after signing in.");
        mailSender.send(message);
    }
}