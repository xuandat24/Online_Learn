package com.onlinelearn.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body);
    void sendVerificationEmail(String to, String token);
    void sendResetPasswordEmail(String to, String token);
}
