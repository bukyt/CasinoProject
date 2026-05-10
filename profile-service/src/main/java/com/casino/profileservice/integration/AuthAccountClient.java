package com.casino.profileservice.integration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

@Component
public class AuthAccountClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.base-url:http://localhost:8090}")
    private String authBaseUrl;

    @Value("${auth.verification.enabled:true}")
    private boolean verificationEnabled;

    public AuthAccountClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void verifyAccountExists(String accountId, String authorizationHeader) {
        if (!verificationEnabled) {
            return;
        }
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token required to create a profile");
        }
        String base = authBaseUrl.endsWith("/") ? authBaseUrl.substring(0, authBaseUrl.length() - 1) : authBaseUrl;
        String url = base + "/auth/accounts/" + accountId;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<?, ?> body = response.getBody();
            Object aid = body != null ? body.get("accountId") : null;
            if (aid == null || !accountId.equals(String.valueOf(aid))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account id mismatch");
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot access this account with current token");
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Auth service unavailable");
        }
    }
}
