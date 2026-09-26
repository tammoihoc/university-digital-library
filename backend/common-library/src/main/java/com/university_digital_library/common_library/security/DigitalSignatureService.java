package com.university_digital_library.common_library.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class DigitalSignatureService {
    
    private static final String ALGORITHM = "SHA256withRSA";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    
    public DigitalSignatureService(
            @Value("${security.rsa.private-key:}") String privateKeyPem,
            @Value("${security.rsa.public-key:}") String publicKeyPem) throws Exception {
        
        if (privateKeyPem != null && !privateKeyPem.isEmpty()) {
            this.privateKey = loadPrivateKey(privateKeyPem);
        } else {
            this.privateKey = null;
        }
        
        if (publicKeyPem != null && !publicKeyPem.isEmpty()) {
            this.publicKey = loadPublicKey(publicKeyPem);
        } else {
            this.publicKey = null;
        }
    }
    
    private PrivateKey loadPrivateKey(String pem) throws Exception {
        String clean = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }
    
    private PublicKey loadPublicKey(String pem) throws Exception {
        String clean = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
        
        byte[] keyBytes = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }
    
    public String signData(String data) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Private key not configured");
        }
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        byte[] signed = signature.sign();
        return Base64.getEncoder().encodeToString(signed);
    }
    
    public boolean verifySignature(String data, String signatureBase64) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Public key not configured");
        }
        Signature signature = Signature.getInstance(ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }
}
