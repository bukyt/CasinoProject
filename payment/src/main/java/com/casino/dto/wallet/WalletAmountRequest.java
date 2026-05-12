package com.casino.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(name = "WalletAmountRequest", description = "Amount to apply to a wallet balance")
public record WalletAmountRequest(
        @Schema(description = "Positive amount with two decimal places", example = "25.50")
        @NotNull
        BigDecimal amount
) {
}
