package com.casino.dto.flag;

import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Compliance flag assigned to a player's compliance profile")
public record ComplianceFlagDto(

    @Schema(description = "Compliance flag ID", example = "789")
    Long flagId,

    @Schema(description = "Compliance profile ID", example = "456")
    Long complianceId,

    @Schema(description = "Compliance flag type", example = "AML_REVIEW")
    ComplianceFlagType type,

    @Schema(description = "Compliance flag severity", example = "HIGH")
    ComplianceFlagSeverity severity,

    @Schema(description = "Date and time when the flag was created", example = "2026-05-03T12:00:00+03:00")
    OffsetDateTime createdDate,

    @Schema(description = "Date and time when the flag was resolved", example = "2026-05-10T12:00:00+03:00")
    OffsetDateTime resolvedDate
) {
    public static final String EXAMPLE = """
        {
          "flagId": 789,
          "complianceId": 456,
          "type": "AML_REVIEW",
          "severity": "HIGH",
          "createdDate": "2026-05-03T12:00:00+03:00",
          "resolvedDate": null
        }
        """;
}