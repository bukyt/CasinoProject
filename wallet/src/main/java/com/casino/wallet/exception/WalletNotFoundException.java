package com.casino.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Integer playerProfileId) {
        super("Wallet not found for playerProfileId " + playerProfileId);
    }
}
