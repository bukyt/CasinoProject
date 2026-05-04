package com.casino.dto.profile;

import com.casino.dto.flag.ComplianceFlagDto;
import com.casino.dto.limit.GamblingLimitDto;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

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
    OffsetDateTime lastReviewDate,

    @Schema(description = "Gambling limits associated with this compliance profile")
    List<GamblingLimitDto> limits,

    @Schema(description = "Compliance flags associated with this compliance profile")
    List<ComplianceFlagDto> flags
) {
    public static final String DESCRIPTION =
        "Compliance profile containing age verification, self-exclusion, AML risk state, gambling limits, and compliance flags for a player.";

    public static final String EXAMPLE = """
        {
          "complianceId": 10,
          "playerProfileId": 123,
          "ageVerified": true,
          "selfExcluded": false,
          "riskLevel": "LOW",
          "lastReviewDate": "2026-05-01T12:30:00+03:00",
          "limits": [
            {
              "limitId": 1,
              "type": "BET",
              "amount": 100,
              "period": "DAILY",
              "createdDate": "2026-05-01T12:30:00+03:00",
              "startDate": "2026-05-01T00:00:00+03:00",
              "endDate": null,
              "revokedDate": null
            }
          ],
          "flags": [
            {
              "flagId": 5,
              "type": "NO_AGE_VERIFICATION",
              "severity": "HIGH",
              "createdDate": "2026-05-01T12:30:00+03:00",
              "resolvedDate": null
            }
          ]
        }
        """;
}