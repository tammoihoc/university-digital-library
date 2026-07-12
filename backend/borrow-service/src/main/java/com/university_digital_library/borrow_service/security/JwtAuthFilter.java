// borrow-service/src/main/java/.../security/JwtAuthFilter.java
package com.university_digital_library.borrow_service.security;

import com.university_digital_library.borrow_service.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }
        
        final String token = authHeader.substring(7);
        log.info("Token received: {}...", token.substring(0, Math.min(50, token.length())));
        
        try {
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.getClaims(token);
                String username = claims.getSubject();
                log.info("Username from token: {}", username);
                
                // LẤY ROLE TỪ TOKEN - KIỂM TRA CẢ 2 CÁCH
                List<String> roles = null;
                
                // Cách 1: Lấy từ "roles"
                if (claims.get("roles") != null) {
                    roles = claims.get("roles", List.class);
                    log.info("Roles from 'roles' claim: {}", roles);
                }
                // Cách 2: Lấy từ "authorities"
                else if (claims.get("authorities") != null) {
                    roles = claims.get("authorities", List.class);
                    log.info("Roles from 'authorities' claim: {}", roles);
                }
                // Cách 3: Lấy từ "scope"
                else if (claims.get("scope") != null) {
                    String scope = claims.get("scope", String.class);
                    roles = List.of(scope.split(" "));
                    log.info("Roles from 'scope' claim: {}", roles);
                }
                
                if (roles == null || roles.isEmpty()) {
                    log.warn("No roles found in token for user: {}", username);
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Tạo authorities với prefix ROLE_
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            String roleStr = role.toString();
                            if (!roleStr.startsWith("ROLE_")) {
                                return new SimpleGrantedAuthority("ROLE_" + roleStr);
                            }
                            return new SimpleGrantedAuthority(roleStr);
                        })
                        .collect(Collectors.toList());
                
                log.info("Authorities created for user {}: {}", username, authorities);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    request.setAttribute("userId", username);
                    String userType = authorities.stream()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .findFirst()
                            .orElse("STUDENT");
                    request.setAttribute("userType", userType);
                    
                    log.info("Authenticated user: {} with role: {}", username, userType);
                }
            } else {
                log.warn("Token validation failed");
            }
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/health") || 
               path.equals("/actuator/health") ||
               path.equals("/test");
    }
}
