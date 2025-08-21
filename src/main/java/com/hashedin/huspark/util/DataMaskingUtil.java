package com.hashedin.huspark.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for masking sensitive data in API responses
 * to prevent accidental exposure of Personally Identifiable Information (PII)
 */
@Component
public class DataMaskingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMaskingUtil.class);
    
    // Email masking pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$");
    
    // Phone number masking pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{1,3}[-\\s]?)?(\\d{3})[-\\s]?(\\d{3})[-\\s]?(\\d{4})$");
    
    /**
     * Masks an email address by showing only the first and last character of the local part
     * Example: jane.doe@example.com -> j***e@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return email;
        }
        
        try {
            var matcher = EMAIL_PATTERN.matcher(email.trim());
            if (matcher.matches()) {
                String localPart = matcher.group(1);
                String domain = matcher.group(2);
                
                if (localPart.length() <= 2) {
                    return email; // Too short to mask meaningfully
                }
                
                String maskedLocalPart = localPart.charAt(0) + 
                                       "*".repeat(localPart.length() - 2) + 
                                       localPart.charAt(localPart.length() - 1);
                
                return maskedLocalPart + "@" + domain;
            }
        } catch (Exception e) {
            logger.warn("Failed to mask email: {}", email, e);
        }
        
        return email; // Return original if masking fails
    }
    
    /**
     * Masks a phone number by showing only the last 4 digits
     * Example: +1-555-123-4567 -> ***-***-4567
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        try {
            var matcher = PHONE_PATTERN.matcher(phone.trim());
            if (matcher.matches()) {
                String lastFour = matcher.group(4);
                return "***-***-" + lastFour;
            }
        } catch (Exception e) {
            logger.warn("Failed to mask phone: {}", phone, e);
        }
        
        return phone; // Return original if masking fails
    }
    
    /**
     * Masks a name by showing only the first and last character
     * Example: John Doe -> J*** D**
     */
    public static String maskName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        try {
            String[] parts = name.trim().split("\\s+");
            StringBuilder maskedName = new StringBuilder();
            
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    maskedName.append(" ");
                }
                
                String part = parts[i];
                if (part.length() <= 2) {
                    maskedName.append(part);
                } else {
                    maskedName.append(part.charAt(0))
                             .append("*".repeat(part.length() - 2))
                             .append(part.charAt(part.length() - 1));
                }
            }
            
            return maskedName.toString();
        } catch (Exception e) {
            logger.warn("Failed to mask name: {}", name, e);
        }
        
        return name; // Return original if masking fails
    }
    
    /**
     * Masks any sensitive string by showing only first and last characters
     * Example: sensitive123 -> s********3
     */
    public static String maskSensitiveString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        String trimmed = value.trim();
        if (trimmed.length() <= 2) {
            return trimmed;
        }
        
        return trimmed.charAt(0) + "*".repeat(trimmed.length() - 2) + trimmed.charAt(trimmed.length() - 1);
    }
    
    /**
     * Determines if a field name is considered sensitive
     */
    public static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        
        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("password") ||
               lowerFieldName.contains("email") ||
               lowerFieldName.contains("phone") ||
               lowerFieldName.contains("ssn") ||
               lowerFieldName.contains("credit") ||
               lowerFieldName.contains("card") ||
               lowerFieldName.contains("secret") ||
               lowerFieldName.contains("token") ||
               lowerFieldName.contains("key");
    }
}
