package com.casino.wallet.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Integer playerProfileId) {
        super("Insufficient funds for playerProfileId " + playerProfileId);
    }
}
