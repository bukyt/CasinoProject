package com.casino.service;

import com.casino.dto.wallet.WalletAmountRequest;
import com.casino.dto.wallet.WalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletClient {

    private final RestClient walletRestClient;

    public WalletResponse getWallet(Long playerProfileId) {
        return walletRestClient.get()
                .uri("/wallet/{playerProfileId}", playerProfileId)
                .retrieve()
                .body(WalletResponse.class);
    }

    public WalletResponse debit(Long playerProfileId, BigDecimal amount) {
        return walletRestClient.post()
                .uri("/wallet/debit/{playerProfileId}", playerProfileId)
                .body(new WalletAmountRequest(amount))
                .retrieve()
                .body(WalletResponse.class);
    }

    public WalletResponse createWallet(Long playerProfileId) {
        return walletRestClient.post()
                .uri("/wallet/create/{playerProfileId}", playerProfileId)
                .retrieve()
                .body(WalletResponse.class);
    }

    public WalletResponse credit(Long playerProfileId, BigDecimal amount) {
        return walletRestClient.post()
                .uri("/wallet/credit/{playerProfileId}", playerProfileId)
                .body(new WalletAmountRequest(amount))
                .retrieve()
                .body(WalletResponse.class);
    }
}