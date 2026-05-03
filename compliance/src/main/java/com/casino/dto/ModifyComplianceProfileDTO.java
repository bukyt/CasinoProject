package com.casino.dto;

import com.casino.model.ComplianceProfileRiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for modifying a compliance profile")
public record ModifyComplianceProfileDTO(

    @Schema(description = "Whether the player's age has been verified", example = "true")
    Boolean ageVerified,

    @Schema(description = "Whether the player is self-excluded", example = "false")
    Boolean selfExcluded,

    @Schema(description = "Compliance risk level", example = "LOW")
    ComplianceProfileRiskLevel riskLevel

) {
    public static final String EXAMPLE = """
        {
          "ageVerified": true,
          "selfExcluded": false,
          "riskLevel": "LOW"
        }
        """;
}