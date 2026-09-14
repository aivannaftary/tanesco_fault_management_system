package com.tanesco.faultmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(
            String username,
            String role,
            String fullName
    ) {

        Map<String, Object> claims = new HashMap<>();

        claims.put("role", role);
        claims.put("fullName", fullName);

        return buildToken(claims, username);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String username
    ) {

        long currentTime = System.currentTimeMillis();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(currentTime))
                .expiration(new Date(currentTime + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {

        Claims claims = extractAllClaims(token);

        return claims.get("role", String.class);
    }

    public String extractFullName(String token) {

        Claims claims = extractAllClaims(token);

        return claims.get("fullName", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(
            String token,
            String username
    ) {

        try {

            String tokenUsername = extractUsername(token);

            return tokenUsername.equals(username)
                    && !isTokenExpired(token);

        } catch (Exception exception) {

            return false;
        }
    }

    public boolean isTokenExpired(String token) {

        Date expiration = extractExpiration(token);

        return expiration.before(new Date());
    }

    private SecretKey getSigningKey() {

        byte[] keyBytes;

        try {

            keyBytes = Decoders.BASE64.decode(jwtSecret);

        } catch (Exception exception) {

            keyBytes = jwtSecret.getBytes();
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}