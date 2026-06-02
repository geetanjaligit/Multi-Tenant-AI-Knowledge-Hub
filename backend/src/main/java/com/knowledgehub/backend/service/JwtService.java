package com.knowledgehub.backend.service;

import com.knowledgehub.backend.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // This is the cryptographic secret used to sign the tokens.
    // It is securely injected from the application.properties file.
    @Value("${jwt.secret}")
    private String secretKey;

    // Strict 24-hour expiration in milliseconds
    private static final long JWT_EXPIRATION_MS = 86400000;

    /**
     * Generates a token specifically for our User entity, adding minimal custom claims.
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("username", user.getUsername());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername()) // The core identifier of the token
                .setIssuedAt(new Date(System.currentTimeMillis())) // Creation time
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS)) // 24-hour expiry
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Cryptographic signature
                .compact();
    }

    /**
     * Extracts the username (subject) from the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the custom userId claim from the token.
     */
    public Long extractUserId(String token) {
        // We explicitly cast the "userId" claim back to a Long
        Number userIdNumber = extractAllClaims(token).get("userId", Number.class);
        return userIdNumber != null ? userIdNumber.longValue() : null;
    }

    /**
     * Validates if the token belongs to the given user and is not expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Helper method to check if the current time is past the token's expiration date.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract a specific piece of data (claim) from the token payload.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the entire token, verifies the signature using our secret key, and reads the payload.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // If the token was tampered with, this will throw an Exception!
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Decodes our base64 secret key and creates an HMAC key for cryptographic signing.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
