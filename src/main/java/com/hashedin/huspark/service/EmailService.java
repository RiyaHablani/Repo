package com.hashedin.huspark.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendEmail(String to, String subject, String message) {
        // Mock email service - in production, this would integrate with a real email service
        log.info("=== EMAIL NOTIFICATION ===");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Message: {}", message);
        log.info("=== END EMAIL ===");
        
        // Simulate email sending delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
