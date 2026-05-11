package com.casino.game;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BonusClient {

    private final RestTemplate restTemplate;

    public BonusClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean hasActiveBonus(String playerId) {
        String url = "http://localhost:8084/bonuses/players/" + playerId + "/has-active-bonus";

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
        String url = "http://localhost:8084/bonuses/players/" + playerId + "/consume";

        System.out.println("BONUS CONSUME REQUEST -> " + url);

        try {
            restTemplate.postForObject(url, null, Object.class);
            System.out.println("BONUS CONSUME OK");
        } catch (Exception e) {
            System.out.println("BONUS CONSUME ERROR -> " + e.getMessage());
        }
    }

    public void grantFreeSpin(String playerId) {
        String url = "http://localhost:8084/bonuses/players/" + playerId + "/grant-free-spin";

        System.out.println("BONUS GRANT REQUEST -> " + url);

        try {
            restTemplate.postForObject(url, null, Object.class);
            System.out.println("BONUS GRANT OK");
        } catch (Exception e) {
            System.out.println("BONUS GRANT ERROR -> " + e.getMessage());
        }
    }
}