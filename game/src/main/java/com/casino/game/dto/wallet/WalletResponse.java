package com.casino.game.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "WalletResponse", description = "Wallet balance for a player profile")
public record WalletResponse(
        @Schema(description = "Player profile identifier", example = "123")
        Integer playerProfileId,
        @Schema(description = "Available wallet balance", example = "97.50")
        BigDecimal availableBalance
) {
}
