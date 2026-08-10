package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${app.brevo.sender-name:MockAI Support}")
    private String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        logger.info("==================================================");
        logger.info("OTP verification code for [{}]: {}", toEmail, otpCode);
        logger.info("==================================================");

        if (senderEmail == null || senderEmail.trim().isEmpty()) {
            logger.warn("Mail sender username not configured. Falling back to Console Logging.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderName + " <" + senderEmail + ">");
            helper.setTo(toEmail);
            helper.setSubject("Verify Your AI Mock Interview Account");
            
            String htmlContent = "<html><body>" +
                    "<h2>Verify Your Account</h2>" +
                    "<p>Thank you for registering. Please use the following 6-digit One-Time Password (OTP) to verify your account:</p>" +
                    "<h1 style='color: #4f46e5; letter-spacing: 2px;'>" + otpCode + "</h1>" +
                    "<p>This code is valid for 10 minutes and can only be used once.</p>" +
                    "</body></html>";
            
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully via SMTP to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send transactional email via SMTP: {}", e.getMessage(), e);
        }
    }
}
