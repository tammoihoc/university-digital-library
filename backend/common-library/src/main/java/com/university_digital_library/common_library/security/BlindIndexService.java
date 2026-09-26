package com.university_digital_library.common_library.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/**
 * Sinh "blind index" cho các cột đang bị mã hóa AES-GCM (random IV) như
 * email, studentId trong UserProfile.
 * <p>
 * Vấn đề: AES-GCM mã hóa cùng 1 giá trị 2 lần ra 2 ciphertext khác nhau
 * (do IV ngẫu nhiên), nên không thể dùng {@code WHERE email = ?} hay
 * UNIQUE constraint trực tiếp trên cột đã mã hóa — {@code findByEmail}
 * gần như luôn không khớp dù email tồn tại.
 * <p>
 * Giải pháp chuẩn (tương tự cách các hệ thống PCI/HIPAA xử lý "searchable
 * encryption"): lưu thêm một cột phụ chứa HMAC-SHA256(giá trị đã chuẩn hóa)
 * bằng một khóa bí mật riêng (KHÔNG dùng lại khóa AES). HMAC là hàm băm có
 * khóa nhưng KHÔNG dùng IV ngẫu nhiên — cùng input + cùng khóa luôn ra cùng
 * output, nên có thể tra cứu bằng {@code WHERE email_hash = ?} và đặt UNIQUE
 * trên cột hash. Bản thân giá trị gốc vẫn không bị lộ vì HMAC không thể đảo
 * ngược, khác với việc lưu chính ciphertext AES (về lý thuyết vẫn có thể giải
 * mã nếu lộ khóa AES) làm cột tra cứu.
 */
@Component
@Slf4j
public class BlindIndexService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final SecretKeySpec hmacKey;

    public BlindIndexService(
        @Value("${security.blind-index.secret-key:cXV5ZW4tZGFpLWhvYy10aHV2aWVuLXNvLWJsaW5kLWluZGV4LWtleQ==}") String base64Key
    ) {
        byte[] decoded = Base64.getDecoder().decode(base64Key);
        this.hmacKey = new SecretKeySpec(decoded, HMAC_ALGORITHM);
    }

    /**
     * Tính blind index cho một giá trị. Chuẩn hóa (trim + lowercase) trước
     * khi băm để "Test@Mail.com" và "test@mail.com" cho cùng 1 hash — tránh
     * tạo 2 bản ghi trùng chỉ khác hoa/thường.
     *
     * @return chuỗi hex 64 ký tự, hoặc {@code null} nếu input null/rỗng.
     */
    public String hash(String plainValue) {
        if (plainValue == null || plainValue.isBlank()) {
            return null;
        }
        try {
            String normalized = plainValue.trim().toLowerCase(Locale.ROOT);
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(hmacKey);
            byte[] result = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (Exception e) {
            log.error("Blind index hashing error: {}", e.getMessage());
            throw new RuntimeException("Failed to compute blind index", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
