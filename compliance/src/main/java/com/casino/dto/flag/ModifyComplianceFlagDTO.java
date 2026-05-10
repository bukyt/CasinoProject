package com.casino.dto.flag;

import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Request body for modifying a compliance flag")
public record ModifyComplianceFlagDTO(

    @Schema(description = "New compliance flag type", example = "AML_REVIEW")
    ComplianceFlagType type,

    @Schema(description = "New compliance flag severity", example = "MEDIUM")
    ComplianceFlagSeverity severity,

    @Schema(description = "Date and time when the flag was resolved", example = "2026-05-10T12:00:00+03:00")
    OffsetDateTime resolvedDate
) {
    public static final String EXAMPLE = """
        {
          "severity": "MEDIUM",
          "resolvedDate": "2026-05-10T12:00:00+03:00"
        }
        """;
}