package com.casino.dto;

import com.casino.model.ComplianceProfileRiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = ComplianceProfileDto.DESCRIPTION)
public record ComplianceProfileDto(

        @Schema(description = "Compliance profile identifier", example = "10")
        Long complianceId,

        @Schema(description = "Player profile identifier", example = "123")
        Long playerProfileId,

        @Schema(description = "Whether the player has passed age verification", example = "true")
        boolean ageVerified,

        @Schema(description = "Whether the player is self-excluded from gambling", example = "false")
        boolean selfExcluded,

        @Schema(description = "AML/compliance risk classification", example = "LOW")
        ComplianceProfileRiskLevel riskLevel,

        @Schema(description = "Date and time of the last compliance review")
        OffsetDateTime lastReviewDate
) {
    public static final String DESCRIPTION =
            "Compliance profile containing age verification, self-exclusion, and AML risk state for a player.";

    public static final String EXAMPLE = """
            {
              "complianceId": 10,
              "playerProfileId": 123,
              "ageVerified": true,
              "selfExcluded": false,
              "riskLevel": "LOW",
              "lastReviewDate": "2026-05-01T12:30:00+03:00"
            }
            """;
}