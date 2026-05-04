package com.casino.dto.limit;

import com.casino.model.limit.GamblingLimitPeriod;
import com.casino.model.limit.GamblingLimitType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Responsible gambling limit assigned to a player's compliance profile")
public record GamblingLimitDto(

    @Schema(description = "Gambling limit ID", example = "456")
    Long limitId,

    @Schema(description = "Compliance profile ID", example = "123")
    Long complianceId,

    @Schema(description = "Gambling limit type", example = "DEPOSIT")
    GamblingLimitType type,

    @Schema(description = "Limit amount", example = "500")
    Integer amount,

    @Schema(description = "Limit period", example = "DAILY")
    GamblingLimitPeriod period,

    @Schema(description = "Date and time when the limit was created", example = "2026-05-03T12:00:00+03:00")
    OffsetDateTime createdDate,

    @Schema(description = "Date and time when the limit becomes active", example = "2026-05-03T12:00:00+03:00")
    OffsetDateTime startDate,

    @Schema(description = "Date and time when the limit ends", example = "2026-06-03T12:00:00+03:00")
    OffsetDateTime endDate,

    @Schema(description = "Date and time when the limit was revoked", example = "2026-05-20T12:00:00+03:00")
    OffsetDateTime revokedDate
) {
    public static final String EXAMPLE = """
        {
          "limitId": 456,
          "complianceId": 123,
          "type": "DEPOSIT",
          "amount": 500,
          "period": "DAILY",
          "createdDate": "2026-05-03T12:00:00+03:00",
          "startDate": "2026-05-03T12:00:00+03:00",
          "endDate": null,
          "revokedDate": null
        }
        """;

    public static final String LIST_EXAMPLE = """
        [
          {
            "limitId": 456,
            "complianceId": 123,
            "type": "DEPOSIT",
            "amount": 500,
            "period": "DAILY",
            "createdDate": "2026-05-03T12:00:00+03:00",
            "startDate": "2026-05-03T12:00:00+03:00",
            "endDate": null,
            "revokedDate": null
          }
        ]
        """;
}