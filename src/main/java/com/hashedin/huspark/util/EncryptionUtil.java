package com.hashedin.huspark.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncryptionUtil {

    @Value("${app.encryption.secret:default-secret-key-for-development}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;
    private static final String ALGORITHM = "AES";

    private SecretKeySpec getSecretKeySpec() {
        if (secretKeySpec == null) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] key = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));
                key = Arrays.copyOf(key, 16); // Use only first 128 bit
                secretKeySpec = new SecretKeySpec(key, ALGORITHM);
            } catch (Exception e) {
                throw new RuntimeException("Error creating secret key", e);
            }
        }
        return secretKeySpec;
    }

    public String encrypt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec());
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting value", e);
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return encrypted;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting value", e);
        }
    }
}
