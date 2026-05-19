package com.casino.profileservice.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Component
public class WalletClient {

    private final RestTemplate restTemplate;

    @Value("${wallet.service.base-url:http://localhost:8085}")
    private String walletBaseUrl;

    public WalletClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createWallet(Integer playerProfileId) {
        String base = walletBaseUrl.endsWith("/") ? walletBaseUrl.substring(0, walletBaseUrl.length() - 1)
                : walletBaseUrl;
        String url = base + "/wallet/create/" + playerProfileId;

        try {
            restTemplate.postForEntity(url, null, Void.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Wallet service unavailable");
        }
    }
}
