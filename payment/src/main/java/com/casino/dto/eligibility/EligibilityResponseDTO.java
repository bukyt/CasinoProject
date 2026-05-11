package com.casino.dto.eligibility;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = EligibilityResponseDTO.DESCRIPTION)
public record EligibilityResponseDTO(

    @Schema(description = "Player profile identifier", example = "123")
    Long playerProfileId,

    @Schema(description = "Whether the player may place bets", example = "true")
    boolean mayBet,

    @Schema(description = "Whether the player may withdraw funds", example = "true")
    boolean mayWithdraw,

    @Schema(description = "Current AML/compliance risk classification", example = "LOW")
    ComplianceProfileRiskLevel riskLevel,

    @Schema(description = "Whether the player has passed age verification", example = "true")
    boolean ageVerified,

    @Schema(description = "Whether the player is self-excluded", example = "false")
    boolean selfExcluded,

    @Schema(description = "Reasons blocking betting or withdrawal")
    List<EligibilityBlockReason> blockReasons,

    @Schema(description = "Current active betting limit, if one exists")
    EligibilityLimitDTO activeBetLimit,

    @Schema(description = "Current active withdrawal limit, if one exists")
    EligibilityLimitDTO activeWithdrawalLimit,

    @Schema(description = "Date and time when eligibility was checked")
    OffsetDateTime checkedAt
) {
    public static final String DESCRIPTION =
        "Eligibility result describing whether a player may bet or withdraw.";

    public static final String EXAMPLE = """
        {
          "playerProfileId": 123,
          "mayBet": true,
          "mayWithdraw": true,
          "riskLevel": "LOW",
          "ageVerified": true,
          "selfExcluded": false,
          "blockReasons": [],
          "activeBetLimit": {
            "amount": 100,
            "period": "DAILY"
          },
          "activeWithdrawalLimit": {
            "amount": 500,
            "period": "WEEKLY"
          },
          "checkedAt": "2026-05-01T12:30:00+03:00"
        }
        """;
}