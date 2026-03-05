package com.example.NotsHub.service;

import com.example.NotsHub.exceptions.APIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailOtpNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromEmail;
    private final boolean logOnlyMode;

    public EmailOtpNotificationService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.email.from:}") String fromEmail,
            @Value("${app.email.otp.log-only:true}") boolean logOnlyMode) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromEmail = fromEmail;
        this.logOnlyMode = logOnlyMode;
    }

    public void sendVerificationOtp(String recipient, String otp, int expiryMinutes) {
        if (logOnlyMode) {
            log.info("OTP for {} is {} (log-only mode enabled)", recipient, otp);
            return;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                throw new APIException("Email sender is not configured.");
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            if (!fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setSubject("NotsHub Email Verification OTP");
            message.setText("Your NotsHub verification OTP is " + otp + ". It expires in "
                    + expiryMinutes + " minutes.");
            mailSender.send(message);
        } catch (Exception ex) {
            throw new APIException("Unable to send verification OTP email. Please try again.");
        }
    }
}
