package com.example.huganstorev2.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.huganstorev2.models.user.entity.Role;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKeyEnv;
    private Key signingKey;

    public JwtService(
            @Value("${jwt.secret-key}") String secretKeyEnv
    ) {
        this.secretKeyEnv = secretKeyEnv;
    }


    @PostConstruct
    public void init() {
        if (secretKeyEnv == null || secretKeyEnv.isBlank()) {
            throw new IllegalStateException("Không tìm thấy SecretKey!");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secretKeyEnv);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ═══════════════════════════════════════════════════
    // 1. SINH TOKEN
    // ═══════════════════════════════════════════════════
    public String generateToken(String email, Role role, String sessionId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name()) 
                .claim("sessionId", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // ═══════════════════════════════════════════════════
    // 2. GIẢI MÃ TOKEN → LẤY EMAIL
    // ═══════════════════════════════════════════════════
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ═══════════════════════════════════════════════════
    // 3. GIẢI MÃ TOKEN → LẤY ROLE
    // ═══════════════════════════════════════════════════
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractSessionId(String token) {
        return extractAllClaims(token).get("sessionId", String.class);
    }

    // ═══════════════════════════════════════════════════
    // 4. KIỂM TRA TOKEN HỢP LỆ (Đúng chữ ký + Còn hạn)
    // ═══════════════════════════════════════════════════
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════════════
    // 5. LẤY TOÀN BỘ CLAIMS (NỘI BỘ)
    // ═══════════════════════════════════════════════════
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
} 