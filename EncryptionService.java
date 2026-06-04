package com.DataPro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // 96 bits
    private static final int GCM_TAG_LENGTH = 128;  // bits

    @Value("${app.encryption.secret-key}")
    private String secretKeyBase64; // Must be a 32-byte (256-bit) Base64 key in properties

    // ── Get AES key from config ──────────────────────────────────────
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        return new SecretKeySpec(keyBytes, "AES");
    }

    // ── Encrypt bytes → Base64(IV + ciphertext) ──────────────────────
    public byte[] encrypt(byte[] plainBytes) throws Exception {
        SecretKey key = getSecretKey();
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encrypted = cipher.doFinal(plainBytes);

        // Prepend IV to ciphertext: [IV (12 bytes)] + [encrypted]
        byte[] combined = new byte[GCM_IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, GCM_IV_LENGTH, encrypted.length);
        return combined;
    }

    // ── Decrypt [IV + ciphertext] → plain bytes ───────────────────────
    public byte[] decrypt(byte[] encryptedWithIv) throws Exception {
        SecretKey key = getSecretKey();

        byte[] iv = Arrays.copyOfRange(encryptedWithIv, 0, GCM_IV_LENGTH);
        byte[] cipherText = Arrays.copyOfRange(encryptedWithIv, GCM_IV_LENGTH, encryptedWithIv.length);

        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(cipherText);
    }

    // ── SHA-256 hash of raw (decrypted) file content ─────────────────
    public String computeSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}