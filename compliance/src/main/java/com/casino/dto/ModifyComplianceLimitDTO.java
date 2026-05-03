package com.casino.dto;

import com.casino.model.GamblingLimitPeriod;
import com.casino.model.GamblingLimitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

@Schema(description = "Request body for modifying a responsible gambling limit")
public record ModifyComplianceLimitDTO(

        @Schema(description = "New gambling limit type", example = "DEPOSIT")
        GamblingLimitType type,

        @Positive
        @Schema(description = "New limit amount", example = "300")
        Integer amount,

        @Schema(description = "New limit period", example = "DAILY")
        GamblingLimitPeriod period,

        @FutureOrPresent
        @Schema(description = "New start date", example = "2026-05-03T12:00:00+03:00")
        OffsetDateTime startDate,

        @FutureOrPresent
        @Schema(description = "New end date", example = "2026-06-03T12:00:00+03:00")
        OffsetDateTime endDate,

        @Schema(description = "Date and time when the limit was revoked", example = "2026-05-20T12:00:00+03:00")
        OffsetDateTime revokedDate
) {
    public static final String EXAMPLE = """
        {
          "amount": 300,
          "period": "DAILY",
          "endDate": "2026-06-03T12:00:00+03:00",
          "revokedDate": null
        }
        """;
}