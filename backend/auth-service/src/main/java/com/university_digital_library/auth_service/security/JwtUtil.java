package com.university_digital_library.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value("${security.jwt.secret}") String secret,
                   @Value("${security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

public String generateToken(String username, Set<String> roles) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles.stream().collect(Collectors.toList())) // roles là ["ADMIN", "LIBRARIAN"]
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
}

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("✅ Token VALID: " + token);
            return true;
        } catch (Exception ex) {
            System.out.println("❌ Token INVALID: " + ex.getMessage());
            return false;
        }
    }
    
    // THÊM METHOD NÀY: validate token với username cụ thể
    public boolean validateTokenForUser(String token, UserDetails userDetails) {
        try {
            String username = getUsernameFromToken(token);
            return (username != null && username.equals(userDetails.getUsername()));
        } catch (Exception ex) {
            System.out.println("❌ Token validation for user failed: " + ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String username = claims.getSubject();
            System.out.println("✅ Extracted username: " + username);
            return username;
        } catch (Exception ex) {
            System.out.println("❌ Error extracting username: " + ex.getMessage());
            return null;
        }
    }
}
