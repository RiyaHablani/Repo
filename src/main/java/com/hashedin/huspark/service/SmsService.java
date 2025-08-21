package com.hashedin.huspark.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    public void sendSms(String phoneNumber, String message) {
        // Mock SMS service - in production, this would integrate with a real SMS service
        log.info("=== SMS NOTIFICATION ===");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("=== END SMS ===");
        
        // Simulate SMS sending delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
