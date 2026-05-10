package com.casino.ledger.service;

public class LedgerEntryNotFoundException extends RuntimeException {

    public LedgerEntryNotFoundException(Integer id) {
        super("Ledger entry not found: " + id);
    }
}
