package com.gymlog.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String to, String token) {
        MimeMessage message = mailSender.createMimeMessage();
        log.info("EmailService received token: {}", token);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Verify your GymLog account");
            helper.setText(
                    "<p>Click the link below to verify your account:</p>" +
                            "<a href='http://localhost:5173/verify?token=" +
                            URLEncoder.encode(token, StandardCharsets.UTF_8) +
                            "'>Verify my account</a>" +
                            "<p>This link expires in 24 hours.</p>",
                    true
            );
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Reset your GymLog password");
            helper.setText(
                    "<p>Click the link below to reset your password:</p>" +
                            "<a href='http://localhost:5173/reset-password?token=" +
                            URLEncoder.encode(token, StandardCharsets.UTF_8) +
                            "'>Reset my password</a>" +
                            "<p>This link expires in 30 minutes.</p>",
                    true
            );
            log.info("Sending password reset email to: {}", to);
            log.info("EmailService reset token: {}", token);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


}