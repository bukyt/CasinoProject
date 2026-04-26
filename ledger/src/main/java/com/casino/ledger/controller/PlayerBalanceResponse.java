package com.casino.ledger.controller;

import java.math.BigDecimal;

public record PlayerBalanceResponse(
        Integer playerProfileId,
        BigDecimal balance
) {
}
