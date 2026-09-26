package com.university_digital_library.gateway_service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class RequestSignatureFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestSignatureFilter.class);

    @Value("${security.rsa.public-key}")
    private String publicKeyPEM;

    @Value("${security.rsa.enabled:true}")
    private boolean signatureEnabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!signatureEnabled) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Bỏ qua kiểm tra chữ ký cho các static resources (ví dụ: uploads)
        if (path.startsWith("/uploads/")) {
            return chain.filter(exchange);
        }

        String signature = request.getHeaders().getFirst("X-Signature");
        String timestampStr = request.getHeaders().getFirst("X-Timestamp");
        String nonce = request.getHeaders().getFirst("X-Nonce");

        if (signature == null || timestampStr == null || nonce == null) {
            log.warn("Blocked request to {}: Missing signature headers", path);
            return handleUnauthorized(exchange, "Missing signature headers (X-Signature, X-Timestamp, X-Nonce)");
        }

        try {
            // 1. Chống Replay Attack: kiểm tra thời gian hết hạn (5 phút)
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - timestamp) > 300000) { // 5 phút
                log.warn("Blocked request to {}: Request expired. Client time: {}, Server time: {}", path, timestamp, currentTime);
                return handleUnauthorized(exchange, "Request expired");
            }

            // 2. Dựng lại chuỗi chuẩn hóa (Canonical String)
            String method = request.getMethod().name();
            String canonicalString = method + ":" + path + ":" + timestampStr + ":" + nonce;

            // 3. Xác minh chữ ký số RSA
            boolean isValid = SecurityCryptographyUtils.verifyRSA(canonicalString, signature, publicKeyPEM);
            if (!isValid) {
                log.warn("Blocked request to {}: Invalid signature", path);
                return handleUnauthorized(exchange, "Invalid signature");
            }

            log.info("Authorized signature for request to: {}", path);
            return chain.filter(exchange);

        } catch (NumberFormatException e) {
            log.warn("Blocked request to {}: Invalid timestamp format", path);
            return handleUnauthorized(exchange, "Invalid timestamp format");
        } catch (Exception e) {
            log.error("Error verifying signature for request to " + path, e);
            return handleUnauthorized(exchange, "Signature verification error: " + e.getMessage());
        }
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format("{\"error\": \"Forbidden\", \"message\": \"%s\"}", message);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Chạy filter này rất sớm trong chuỗi filter
        return -100;
    }
}
