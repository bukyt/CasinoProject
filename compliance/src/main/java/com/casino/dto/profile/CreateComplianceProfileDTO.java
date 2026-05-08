package com.casino.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for creating a compliance profile")
public record CreateComplianceProfileDTO(

    @NotNull
    @Positive
    @Schema(description = "Player profile ID", example = "123")
    Long playerProfileId

) {
    public static final String EXAMPLE = """
        {
          "playerProfileId": 123
        }
        """;
}