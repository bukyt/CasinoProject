package com.casino.game;

import com.casino.game.dto.wallet.WalletAmountRequest;
import com.casino.game.dto.wallet.WalletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class WalletClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    // Spring injects the property here dynamically, falling back to 8085 if missing
    public WalletClient(RestTemplate restTemplate, 
                        @Value("${services.wallet.base-url:http://localhost:8085}") String walletBaseUrl) {
        this.restTemplate = restTemplate;
        // This ensures the path appends "/wallet" correctly to your configured host
        this.baseUrl = walletBaseUrl + "/wallet";
    }

    public WalletResponse getWallet(Long playerProfileId) {
        String url = baseUrl + "/" + playerProfileId;
        System.out.println("WALLET GET REQUEST -> " + url);

        try {
            WalletResponse response = restTemplate.getForObject(url, WalletResponse.class);
            System.out.println("WALLET GET RESPONSE -> " + response);
            return response;
        } catch (Exception e) {
            System.out.println("WALLET GET ERROR -> " + e.getMessage());
            return null;
        }
    }

    public WalletResponse debit(Long playerProfileId, BigDecimal amount) {
        String url = baseUrl + "/debit/" + playerProfileId;
        System.out.println("WALLET DEBIT REQUEST -> " + url + " WITH AMOUNT: " + amount);

        try {
            WalletAmountRequest requestBody = new WalletAmountRequest(amount);
            WalletResponse response = restTemplate.postForObject(url, requestBody, WalletResponse.class);
            System.out.println("WALLET DEBIT OK -> " + response);
            return response;
        } catch (Exception e) {
            System.out.println("WALLET DEBIT ERROR -> " + e.getMessage());
            return null;
        }
    }

    public WalletResponse credit(Long playerProfileId, BigDecimal amount) {
        String url = baseUrl + "/credit/" + playerProfileId;
        System.out.println("WALLET CREDIT REQUEST -> " + url + " WITH AMOUNT: " + amount);

        try {
            WalletAmountRequest requestBody = new WalletAmountRequest(amount);
            WalletResponse response = restTemplate.postForObject(url, requestBody, WalletResponse.class);
            System.out.println("WALLET CREDIT OK -> " + response);
            return response;
        } catch (Exception e) {
            System.out.println("WALLET CREDIT ERROR -> " + e.getMessage());
            return null;
        }
    }

    public WalletResponse createWallet(Long playerProfileId) {
        String url = baseUrl + "/create/" + playerProfileId;
        System.out.println("WALLET CREATE REQUEST -> " + url);

        try {
            WalletResponse response = restTemplate.postForObject(url, null, WalletResponse.class);
            System.out.println("WALLET CREATE OK -> " + response);
            return response;
        } catch (Exception e) {
            System.out.println("WALLET CREATE ERROR -> " + e.getMessage());
            return null;
        }
    }
}