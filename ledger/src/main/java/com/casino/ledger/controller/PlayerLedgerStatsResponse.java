package com.casino.ledger.controller;

import java.math.BigDecimal;

public record PlayerLedgerStatsResponse(
        Integer playerProfileId,
        BigDecimal totalDeposits,
        BigDecimal totalWithdrawals,
        BigDecimal totalBets,
        BigDecimal totalWins,
        BigDecimal totalLosses
) {
}
