package com.casino.api;

import com.casino.dto.ComplianceProfileDto;
import com.casino.dto.CreateComplianceProfileDTO;
import com.casino.dto.EligibilityResponseDTO;
import com.casino.dto.ModifyComplianceProfileDTO;
import com.casino.exceptions.ApiException;
import com.casino.exceptions.profile.ComplianceProfileExistsException;
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
        name = "Compliance",
        description = "Compliance profile, eligibility, AML risk, and player restriction endpoints"
)
@RequestMapping("/compliance")
public interface ComplianceApi {

    @Operation(
            summary = "Create compliance profile",
            description = "Creates a compliance profile for a player."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Compliance profile created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ComplianceProfileDto.class),
                examples = @ExampleObject(
                    name = "compliance.profile.created",
                    summary = "Created compliance profile",
                    description = "Returned after successfully creating a compliance profile.",
                    value = ComplianceProfileDto.EXAMPLE
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
            responseCode = "409",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class),
                examples = @ExampleObject(
                        name = "compliance.profile.exists",
                        summary = "Compliance profile already exists",
                        description = "The request tried to create a duplicate compliance profile for the same player.",
                        value = ComplianceProfileExistsException.EXAMPLE
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<ComplianceProfileDto> createComplianceProfile(
            @Valid @RequestBody CreateComplianceProfileDTO request
    );

    @Operation(
            summary = "Get compliance profile",
            description = "Returns the compliance profile for a player."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Compliance profile returned",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ComplianceProfileDto.class),
                examples = @ExampleObject(
                        name = "compliance.profile",
                        summary = "Compliance profile",
                        description = "The compliance profile for the requested player.",
                        value = ComplianceProfileDto.EXAMPLE
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
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
        )
    })
    @GetMapping("/{playerId}")
    ResponseEntity<ComplianceProfileDto> getComplianceProfile(
            @Parameter(description = "Player profile ID", example = "123")
            @PathVariable Long playerId
    );

    @Operation(
            summary = "Modify compliance profile",
            description = "Partially updates a compliance profile. Only provided fields are modified."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Compliance profile updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ComplianceProfileDto.class),
                examples = @ExampleObject(
                        name = "compliance.profile.updated",
                        summary = "Updated compliance profile",
                        description = "Returned after successfully modifying a compliance profile.",
                        value = ComplianceProfileDto.EXAMPLE
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
        )
    })
    @PatchMapping("/{playerId}")
    ResponseEntity<ComplianceProfileDto> modifyComplianceProfile(
            @Parameter(description = "Player profile ID", example = "123")
            @PathVariable Long playerId,

            @Valid @RequestBody ModifyComplianceProfileDTO request
    );

    @Operation(
            summary = "Check player eligibility",
            description = "Checks whether a player may bet or withdraw."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Eligibility result returned",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = EligibilityResponseDTO.class),
                examples = @ExampleObject(
                    name = "compliance.eligibility",
                    summary = "Player eligibility",
                    description = "Returned when eligibility is successfully evaluated.",
                    value = EligibilityResponseDTO.EXAMPLE
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
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
        )
    })
    @GetMapping("/{playerId}/eligibility")
    ResponseEntity<EligibilityResponseDTO> checkEligibility(
            @Parameter(description = "Player profile ID", example = "123")
            @PathVariable Long playerId
    );
}