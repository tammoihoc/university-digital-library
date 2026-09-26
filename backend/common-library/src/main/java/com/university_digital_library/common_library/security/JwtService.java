package com.university_digital_library.common_library.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    
    private final SecretKey secretKey;
    private final long expirationMs;
    
    public JwtService(
            @Value("${security.jwt.secret:mySuperSecretKeyForJWTGenerationThatIsAtLeast32CharactersLong}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }
    
    public String generateToken(String username, Set<String> roles) {
        long now = System.currentTimeMillis();
        
        List<String> roleNames = roles.stream()
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .collect(Collectors.toList());
        
        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roleNames)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expirationMs))
            .signWith(secretKey)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaims(token).get("roles", List.class);
    }
    
    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
