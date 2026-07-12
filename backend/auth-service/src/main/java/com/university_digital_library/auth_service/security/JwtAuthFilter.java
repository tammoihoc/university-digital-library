package com.university_digital_library.auth_service.security;

import com.university_digital_library.auth_service.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;  // ← THÊM DÒNG NÀY
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain)
            throws IOException, ServletException {  // ← THÊM ServletException ở đây

        final String authHeader = request.getHeader("Authorization");
        System.out.println("🔍 Authorization Header: " + authHeader);
        
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("🔍 Token extracted: " + token);
            
            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.getUsernameFromToken(token);
                System.out.println("🔍 Username from token: " + username);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // KIỂM TRA THÊM: xem token có thực sự khớp với user không
            if (!jwtUtil.validateTokenForUser(token, userDetails)) {
                System.out.println("❌ Token không khớp với user!");
                filterChain.doFilter(request, response);
                return;
            }
            
            System.out.println("🔍 UserDetails loaded: " + userDetails.getUsername());
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("✅ Authentication set in SecurityContext");
        } else {
            System.out.println("❌ Authentication NOT set");
        }

        filterChain.doFilter(request, response);
    }
}
