package com.casino.authservice.token;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenBlocklistService {

    private final ConcurrentHashMap<String, Long> jtiExpiresAtEpochMs = new ConcurrentHashMap<>();

    public void blockUntil(String jti, Instant expiresAt) {
        if (jti != null && expiresAt != null) {
            jtiExpiresAtEpochMs.put(jti, expiresAt.toEpochMilli());
        }
    }

    public boolean isBlocked(String jti) {
        if (jti == null) {
            return false;
        }
        Long exp = jtiExpiresAtEpochMs.get(jti);
        if (exp == null) {
            return false;
        }
        if (System.currentTimeMillis() > exp) {
            jtiExpiresAtEpochMs.remove(jti);
            return false;
        }
        return true;
    }
}
