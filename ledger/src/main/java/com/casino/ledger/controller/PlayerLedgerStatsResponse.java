package com.casino.ledger.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "PlayerLedgerStatsResponse", description = "Aggregated ledger totals for a player")
public record PlayerLedgerStatsResponse(
        @Schema(description = "Player profile identifier", example = "123")
        Integer playerProfileId,
        @Schema(description = "Sum of all deposits", example = "200.00")
        BigDecimal totalDeposits,
        @Schema(description = "Sum of all withdrawals", example = "40.00")
        BigDecimal totalWithdrawals,
        @Schema(description = "Sum of all bets placed", example = "30.00")
        BigDecimal totalBets,
        @Schema(description = "Sum of all winning settlements", example = "15.00")
        BigDecimal totalWins,
        @Schema(description = "Sum of all losing settlements", example = "7.50")
        BigDecimal totalLosses
) {
}
