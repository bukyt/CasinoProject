package com.casino.config;

import com.casino.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtAuthFilter(@Value("${jwt.secret}") String secretBase64) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    private static String getStringClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);

        if (value == null) {
            return null;
        }

        if (value instanceof String string && !string.isBlank()) {
            return string;
        }

        return String.valueOf(value);
    }

    private static Collection<? extends GrantedAuthority> parseRoles(Object rolesClaim) {
        if (rolesClaim == null) {
            return Collections.emptyList();
        }

        if (rolesClaim instanceof String rolesCsv) {
            return Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(JwtAuthFilter::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .toList();
        }

        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(JwtAuthFilter::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .toList();
        }

        return Collections.emptyList();
    }

    private static String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (
            header != null
                && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null
        ) {
            try {
                String token = header.substring(7);

                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                String accountId = getStringClaim(claims, "accountId");

                if (accountId != null) {
                    AuthenticatedUser principal = new AuthenticatedUser(accountId);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            parseRoles(claims.get("roles"))
                        );

                    authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}