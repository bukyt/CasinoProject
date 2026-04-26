package com.casino.ledger.controller;

import com.casino.ledger.model.LedgerEntry;
import com.casino.ledger.model.LedgerEntryType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntryResponse(
        Integer id,
        Integer playerProfileId,
        LedgerEntryType type,
        BigDecimal amount,
        LocalDate createdDate
) {
    public static LedgerEntryResponse from(LedgerEntry ledgerEntry) {
        return new LedgerEntryResponse(
                ledgerEntry.getEntryId(),
                ledgerEntry.getPlayerProfileId(),
                ledgerEntry.getType(),
                ledgerEntry.getAmount(),
                ledgerEntry.getCreatedDate().toLocalDate()
        );
    }
}
