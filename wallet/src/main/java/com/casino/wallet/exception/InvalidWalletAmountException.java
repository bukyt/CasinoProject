package com.casino.wallet.exception;

public class InvalidWalletAmountException extends RuntimeException {

    public InvalidWalletAmountException() {
        super("Amount must be greater than zero");
    }
}
