package com.dgj.portfolio.web.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long validityInMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long validityInMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMs = validityInMs;
    }

    public String createToken(String email, String fullName) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("name", fullName)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    public long getValidity() {
        return validityInMs;
    }

    public String generateUniqueUsername(String name, String email) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }

        String randomSuffix = UUID.randomUUID().toString().substring(0, 6); // 6-char unique ID
        return base + "_" + randomSuffix;
    }

}

