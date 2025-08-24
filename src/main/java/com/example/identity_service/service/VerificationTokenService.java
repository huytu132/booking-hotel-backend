package com.example.identity_service.service;

import com.example.identity_service.entity.User;
import com.example.identity_service.entity.VerificationToken;
import com.example.identity_service.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    // Tạo token xác thực và gửi email
    public void createAndSendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .type("EMAIL_VERIFY")
                .expiryDate(LocalDateTime.now().plusHours(24)) // Hết hạn sau 24h
                .user(user)
                .build();

        verificationTokenRepository.save(verificationToken);

        // Gửi email xác thực
        String verificationUrl = "http://localhost:8080/auth/verify?token=" + token;
        sendVerificationEmail(user.getEmail(), verificationUrl);
    }

    // Gửi email xác thực
    private void sendVerificationEmail(String email, String verificationUrl) {
        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);
        emailService.sendEmailWithHtmlTemplate(
                email,
                "Verify Your Email Address",
                "email-verification", // Tên template HTML
                context
        );
    }

    // Xác thực token
    public boolean verifyToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(t -> {
                    User user = t.getUser();
                    user.setVerified(true);
                    verificationTokenRepository.delete(t); // Xóa token sau khi xác thực
                    return true;
                })
                .orElse(false);
    }
}