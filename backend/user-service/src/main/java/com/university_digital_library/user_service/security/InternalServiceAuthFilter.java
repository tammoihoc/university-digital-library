package com.university_digital_library.user_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Xác thực request tới từ các service nội bộ (borrow-service, entry_exit_service...)
 * qua header X-Internal-Key, thay cho cách làm cũ là permitAll() hoàn toàn trên
 * /users/{id}/borrow-info và /users/{id}/borrow-count — permitAll cũ cho phép BẤT KỲ AI,
 * kể cả không đăng nhập, gọi thẳng API để đọc thông tin mượn sách hoặc SỬA
 * currentBorrowed của bất kỳ user nào (updateBorrowCount không có kiểm tra
 * quyền nào cả), có thể bị lợi dụng để bypass giới hạn mượn sách hoặc khóa
 * tài khoản người khác bằng cách set số sách đang mượn lên rất cao.
 *
 * Nếu header khớp secret key, request được gán quyền ROLE_INTERNAL — dùng để
 * cho phép các endpoint giao tiếp giữa service mà KHÔNG cần forward JWT của
 * người dùng (ví dụ borrow-service gọi updateBorrowCount khi trả sách, lúc đó
 * không phải lúc nào cũng có sẵn JWT hợp lệ để forward).
 */
@Component
@Slf4j
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    @Value("${security.internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        String providedKey = request.getHeader("X-Internal-Key");

        if (providedKey != null && !providedKey.isBlank()
                && internalApiKey != null && constantTimeEquals(providedKey, internalApiKey)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"));
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken("internal-service", null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authenticated internal service call to {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    // So sánh thời gian không đổi để tránh timing attack khi so sánh secret key
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
