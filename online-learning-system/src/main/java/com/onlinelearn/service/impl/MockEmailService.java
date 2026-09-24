package com.onlinelearn.service.impl;

import com.onlinelearn.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockEmailService implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("""
                
                ========================= [MOCK EMAIL SERVICE] =========================
                📧 To: {}
                📌 Subject: {}
                📝 Content:
                {}
                ========================================================================
                """, to, subject, body);
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String body = "Xin chào! Cảm ơn bạn đã đăng ký. Mã xác thực của bạn là: " + token
                + "\nHoặc truy cập: http://localhost:8080/auth/verify?token=" + token;
        sendEmail(to, "Xác thực tài khoản Online Learning", body);
    }

    @Override
    public void sendResetPasswordEmail(String to, String token) {
        String body = "Xin chào! Bạn đã yêu cầu đặt lại mật khẩu. Mã khôi phục của bạn là: " + token
                + "\nHoặc truy cập: http://localhost:8080/auth/reset-password?token=" + token;
        sendEmail(to, "Yêu cầu đặt lại mật khẩu", body);
    }
}
