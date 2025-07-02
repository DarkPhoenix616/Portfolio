package com.dgj.portfolio.web.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

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

    // 1. Create token with roles, name, subject (email), issue time, expiry
    public String createToken(String email, String fullName, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(email)
                .claim("email", email) // Optional redundant email field
                .claim("name", fullName)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }

    // 2. Safely parse and verify token signature and expiration
    public Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // 3. Extract all useful claims from a JWT
    public Map<String, Object> extractAllClaims(String token) {
        try {
            Claims claims = parseToken(token).getBody();

            Map<String, Object> extracted = new HashMap<>();
            extracted.put("email", claims.getSubject());
            extracted.put("name", claims.get("name"));
            extracted.put("roles", extractRoles(claims));
            extracted.put("issuedAt", claims.getIssuedAt());
            extracted.put("expiration", claims.getExpiration());

            return extracted;

        } catch (JwtException e) {
            // Log if needed
            System.err.println("Invalid token: " + e.getMessage());
            return Collections.emptyMap(); // or throw custom exception
        }
    }

    public String getEmailFromToken(String token) {
        try {
            // Trim in case token has accidental whitespace
            token = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

            return parseToken(token).getBody().get("email", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error extracting email from token: " + e.getMessage());
            return null;
        }
    }


    // 5. Extract roles as list of strings
    public List<String> getRolesFromToken(String token) {
        Object rawRoles = extractAllClaims(token).get("roles");
        if (rawRoles instanceof List<?>) {
            List<?> rawList = (List<?>) rawRoles;
            return rawList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    // Helper to extract from Claims directly
    private List<String> extractRoles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?>) {
            return ((List<?>) raw).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }
        return List.of();
    }

    // 6. Token expiry config
    public long getValidity() {
        return validityInMs;
    }

    // 7. Generate unique usernames (e.g., for OAuth sign-up)
    public String generateUniqueUsername(String name, String email) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) {
            base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6);
        return base + "_" + randomSuffix;
    }
}
