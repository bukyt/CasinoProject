package com.casino.ledger.controller;

import com.casino.ledger.model.LedgerEntry;
import com.casino.ledger.model.LedgerEntryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "LedgerEntryResponse", description = "A persisted ledger entry")
public record LedgerEntryResponse(
        @Schema(description = "Ledger entry identifier", example = "11")
        Integer id,
        @Schema(description = "Player profile identifier", example = "456")
        Integer playerProfileId,
        @Schema(description = "Ledger entry classification", example = "PLAYER_WIN")
        LedgerEntryType type,
        @Schema(description = "Monetary amount attached to the entry", example = "99.99")
        BigDecimal amount,
        @Schema(description = "Date when the entry was created", example = "2026-04-25")
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
