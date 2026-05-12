package com.casino.service;

import com.casino.dto.CreatePaymentDto;
import com.casino.dto.PaymentNotAllowedReason;
import com.casino.dto.wallet.WalletResponse;
import com.casino.exceptions.PaymentNotAllowedException;
import com.casino.model.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class PaymentWalletValidator {

    private final WalletClient walletClient;

    public void validatePaymentAllowed(CreatePaymentDto request, PaymentType type) {
        switch (type) {
            case WITHDRAWAL -> validateWithdrawal(request);
            case DEPOSIT -> validateDeposit(request);
            default -> throw new IllegalArgumentException("Unsupported payment type: " + type);
        }
    }

    private void validateWithdrawal(CreatePaymentDto request) {
        WalletResponse wallet = walletClient.getWallet(request.playerProfileId());

        if (wallet.availableBalance().compareTo(request.amount()) < 0) {
            throw new PaymentNotAllowedException(
                    request.playerProfileId(),
                    PaymentType.WITHDRAWAL,
                    PaymentNotAllowedReason.INSUFFICIENT_FUNDS
            );
        }
    }

    private void validateDeposit(CreatePaymentDto request) {
        ensureWalletExists(request.playerProfileId());
    }

    private WalletResponse ensureWalletExists(Long playerProfileId) {
        try {
            return walletClient.getWallet(playerProfileId);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return walletClient.createWallet(playerProfileId);
            }

            throw ex;
        }
    }
}