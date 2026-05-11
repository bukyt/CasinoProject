package com.casino.dto.eligibility;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Active eligibility limit value.")
public record EligibilityLimitDTO(

    @Schema(description = "Maximum allowed amount for this limit", example = "100")
    int amount,

    @Schema(description = "Limit period", example = "DAILY")
    GamblingLimitPeriod period
) {
}