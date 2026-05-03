package com.casino.api;

import com.casino.dto.ComplianceFlagDto;
import com.casino.dto.CreateComplianceFlagDTO;
import com.casino.dto.ModifyComplianceFlagDTO;
import com.casino.exceptions.ApiException;
import com.casino.exceptions.flag.ComplianceFlagExistsException;
import com.casino.exceptions.flag.ComplianceFlagMissingException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Compliance Flags",
    description = "Compliance, AML, fraud, risk, and restriction flags for players"
)
@RequestMapping("/compliance/{playerId}/flag")
public interface ComplianceFlagApi {

    @Operation(
        summary = "Create compliance flag",
        description = "Creates a compliance flag for a player."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Compliance flag created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ComplianceFlagDto.class),
                examples = @ExampleObject(
                    name = "compliance.flag.created",
                    summary = "Created compliance flag",
                    description = "Returned after successfully creating a compliance flag.",
                    value = ComplianceFlagDto.EXAMPLE
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Compliance profile missing",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class),
                examples = @ExampleObject(
                    name = "compliance.profile.missing",
                    summary = "Compliance profile missing",
                    description = "The requested player does not have a compliance profile.",
                    value = ComplianceProfileMissingException.EXAMPLE
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Compliance flag already exists or conflicts with an active flag",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class),
                examples = @ExampleObject(
                    name = "compliance.flag.exists",
                    summary = "Compliance flag already exists",
                    description = "The request tried to create a duplicate or conflicting compliance flag.",
                    value = ComplianceFlagExistsException.EXAMPLE
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<ComplianceFlagDto> createComplianceFlag(
        @Parameter(description = "Player profile ID", example = "123")
        @PathVariable Long playerId,

        @Valid @RequestBody CreateComplianceFlagDTO request
    );

    @Operation(
        summary = "Modify flag",
        description = "Partially updates a compliance flag. Only provided fields are modified."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Compliance flag updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ComplianceFlagDto.class),
                examples = @ExampleObject(
                    name = "compliance.flag.updated",
                    summary = "Updated compliance flag",
                    description = "Returned after successfully modifying a compliance flag.",
                    value = ComplianceFlagDto.EXAMPLE
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Compliance profile or flag missing",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class),
                examples = {
                    @ExampleObject(
                        name = "compliance.profile.missing",
                        summary = "Compliance profile missing",
                        description = "The requested player does not have a compliance profile.",
                        value = ComplianceProfileMissingException.EXAMPLE
                    ),
                    @ExampleObject(
                        name = "compliance.flag.missing",
                        summary = "Compliance flag missing",
                        description = "The requested compliance flag does not exist.",
                        value = ComplianceFlagMissingException.EXAMPLE
                    )
                }
            )
        )
    })
    @PatchMapping("/{flagId}")
    ResponseEntity<ComplianceFlagDto> modifyComplianceFlag(
        @Parameter(description = "Player profile ID", example = "123")
        @PathVariable Long playerId,

        @Parameter(description = "Compliance flag ID", example = "789")
        @PathVariable Long flagId,

        @Valid @RequestBody ModifyComplianceFlagDTO request
    );
}