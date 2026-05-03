package com.casino.dto;

import com.casino.model.ComplianceFlagSeverity;
import com.casino.model.ComplianceFlagType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for creating a compliance flag")
public record CreateComplianceFlagDTO(

        @NotNull
        @Schema(description = "Compliance flag type", example = "AML_REVIEW")
        ComplianceFlagType type,

        @NotNull
        @Schema(description = "Compliance flag severity", example = "HIGH")
        ComplianceFlagSeverity severity
) {
    public static final String EXAMPLE = """
        {
          "type": "AML_REVIEW",
          "severity": "HIGH"
        }
        """;
}