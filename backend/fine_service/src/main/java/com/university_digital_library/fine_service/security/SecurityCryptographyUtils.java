package com.university_digital_library.fine_service.security;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SecurityCryptographyUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128; // bits

    // ==========================================
    // AES Symmetric Encryption (AES-256-GCM)
    // ==========================================

    public static String encryptAES(String plainText, String secretKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Prepend IV to ciphertext
        byte[] encryptedWithIv = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedWithIv, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
    }

    public static String decryptAES(String encryptedBase64, String secretKeyBase64) throws Exception {
        byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedBase64);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedWithIv, 0, iv, 0, GCM_IV_LENGTH);

        int encryptedSize = encryptedWithIv.length - GCM_IV_LENGTH;
        byte[] encryptedBytes = new byte[encryptedSize];
        System.arraycopy(encryptedWithIv, GCM_IV_LENGTH, encryptedBytes, 0, encryptedSize);

        byte[] keyBytes = Base64.getDecoder().decode(secretKeyBase64);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, AES_ALGORITHM);

        Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // ==========================================
    // RSA Digital Signature (RSA + SHA256)
    // ==========================================

    public static String signRSA(String data, String privateKeyPEM) throws Exception {
        String cleanPrivateKeyPEM = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] privateKeyBytes = Base64.getDecoder().decode(cleanPrivateKeyPEM);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signedBytes = signature.sign();

        return Base64.getEncoder().encodeToString(signedBytes);
    }

    public static boolean verifyRSA(String data, String signatureBase64, String publicKeyPEM) throws Exception {
        String cleanPublicKeyPEM = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] publicKeyBytes = Base64.getDecoder().decode(cleanPublicKeyPEM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

        return signature.verify(signatureBytes);
    }
}
