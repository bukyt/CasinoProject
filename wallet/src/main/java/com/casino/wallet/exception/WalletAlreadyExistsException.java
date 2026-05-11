package com.casino.wallet.exception;

public class WalletAlreadyExistsException extends RuntimeException {

    public WalletAlreadyExistsException(Integer playerProfileId) {
        super("Wallet already exists for playerProfileId " + playerProfileId);
    }
}
