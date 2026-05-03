package com.casino.dto;

import com.casino.model.GamblingLimitPeriod;
import com.casino.model.GamblingLimitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

@Schema(description = "Request body for creating a responsible gambling limit")
public record CreateComplianceLimitDTO(

        @NotNull
        @Schema(description = "Gambling limit type", example = "DEPOSIT")
        GamblingLimitType type,

        @NotNull
        @Positive
        @Schema(description = "Limit amount", example = "500")
        Integer amount,

        @NotNull
        @Schema(description = "Limit period", example = "DAILY")
        GamblingLimitPeriod period,

        @NotNull
        @FutureOrPresent
        @Schema(description = "Date and time when the limit becomes active", example = "2026-05-03T12:00:00+03:00")
        OffsetDateTime startDate,

        @FutureOrPresent
        @Schema(description = "Optional date and time when the limit ends", example = "2026-06-03T12:00:00+03:00")
        OffsetDateTime endDate
) {
    public static final String EXAMPLE = """
        {
          "type": "DEPOSIT",
          "amount": 500,
          "period": "DAILY",
          "startDate": "2026-05-03T12:00:00+03:00",
          "endDate": null
        }
        """;
}