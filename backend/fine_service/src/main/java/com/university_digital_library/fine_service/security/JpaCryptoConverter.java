package com.university_digital_library.fine_service.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Converter
@Component
public class JpaCryptoConverter implements AttributeConverter<String, String> {

    private static String secretKey;

    @Value("${security.aes.secret-key:MDEyMzQ1Njc4OWFiY2RlZmdoaWprbG1ub3BxcnN0dXY=}")
    public void setSecretKey(String key) {
        secretKey = key;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return SecurityCryptographyUtils.encryptAES(attribute, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting field: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return SecurityCryptographyUtils.decryptAES(dbData, secretKey);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting field: " + e.getMessage(), e);
        }
    }
}
