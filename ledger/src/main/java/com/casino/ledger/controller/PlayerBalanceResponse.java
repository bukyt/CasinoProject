package com.casino.ledger.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PlayerBalanceResponse", description = "Current balance for a player")
public record PlayerBalanceResponse(
        @Schema(description = "Player profile identifier", example = "123")
        Integer playerProfileId,
        @Schema(description = "Computed balance across all ledger entries", example = "97.50")
        BigDecimal balance
) {
}
