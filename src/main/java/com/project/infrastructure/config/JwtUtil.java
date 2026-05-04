package com.project.infrastructure.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Jwts.SIG.HS256.key().build().getEncoded());
    }

    public String generateToken(String username, String role) {
        return generateToken(username, role, null);
    }

    public String generateToken(String username, String role, Long jugadorId) {
        var builder = Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs));
        if (jugadorId != null) {
            builder.claim("jugadorId", jugadorId);
        }
        return builder.signWith(key).compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Long extractJugadorId(String token) {
        Number val = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("jugadorId", Number.class);
        return val != null ? val.longValue() : null;
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
