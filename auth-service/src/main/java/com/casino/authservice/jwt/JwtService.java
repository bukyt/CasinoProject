package com.casino.authservice.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.casino.authservice.token.TokenBlocklistService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final TokenBlocklistService tokenBlocklistService;

    @Value("${jwt.secret}")
    private String secretBase64;

    private static final long JWT_VALIDITY_MS = 1000L * 60 * 60;

    private Key signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String accountId, String rolesCommaSeparated) {
        String jti = UUID.randomUUID().toString();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", rolesCommaSeparated);
        claims.put("accountId", accountId);
        claims.put("jti", jti);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_VALIDITY_MS))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String jti = claims.get("jti", String.class);
            return !tokenBlocklistService.isBlocked(jti);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String jti = claims.get("jti", String.class);
            Date exp = claims.getExpiration();
            if (jti != null && exp != null) {
                tokenBlocklistService.blockUntil(jti, exp.toInstant());
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
