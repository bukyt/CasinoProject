package com.casino.game;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BonusClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // FIX: Inject the base URL with environment property parsing and a local fallback
    public BonusClient(RestTemplate restTemplate,
                       @Value("${services.bonus.base-url:http://localhost:8084}") String bonusBaseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = bonusBaseUrl + "/bonuses/players/";
    }

    public boolean hasActiveBonus(String playerId) {
        // FIX: Dynamic string concatenation built from your Docker environment properties
        String url = baseUrl + playerId + "/has-active-bonus";

        System.out.println("BONUS CHECK REQUEST -> " + url);

        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            System.out.println("BONUS CHECK RESPONSE -> " + result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            System.out.println("BONUS CHECK ERROR -> " + e.getMessage());
            return false;
        }
    }

    public void consumeBonus(String playerId) {
        String url = baseUrl + playerId + "/consume";

        System.out.println("BONUS CONSUME REQUEST -> " + url);

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            System.out.println("BONUS CONSUME OK -> " + response);
        } catch (Exception e) {
            System.out.println("BONUS CONSUME ERROR -> " + e.getMessage());
        }
    }

    public void grantFreeSpin(String playerId) {
        String url = baseUrl + playerId + "/grant-free-spin";

        System.out.println("BONUS GRANT REQUEST -> " + url);

        try {
            String response = restTemplate.postForObject(url, null, String.class);
            System.out.println("BONUS GRANT OK -> " + response);
        } catch (Exception e) {
            System.out.println("BONUS GRANT ERROR -> " + e.getMessage());
        }
    }
}