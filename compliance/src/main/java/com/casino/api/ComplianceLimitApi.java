package com.casino.api;

import com.casino.dto.limit.CreateGamblingLimitDTO;
import com.casino.dto.limit.GamblingLimitDto;
import com.casino.dto.limit.ModifyGamblingLimitDTO;
import com.casino.exceptions.ApiException;
import com.casino.exceptions.limit.ComplianceLimitExistsException;
import com.casino.exceptions.limit.ComplianceLimitMissingException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@Tag(
    name = "Compliance Limits",
    description = "Responsible gambling limits for player compliance profiles"
)
@RequestMapping("/compliance/{playerId}/limits")
public interface ComplianceLimitApi {

    @Operation(
        summary = "Create gambling limit",
        description = "Creates a gambling limit for a player."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Compliance limit created",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GamblingLimitDto.class),
                examples = @ExampleObject(
                    name = "compliance.limit.created",
                    summary = "Created compliance limit",
                    description = "Returned after successfully creating a gambling limit.",
                    value = GamblingLimitDto.EXAMPLE
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
            description = "Compliance limit already exists or conflicts with an existing limit",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ApiException.class),
                examples = @ExampleObject(
                    name = "compliance.limit.exists",
                    summary = "Compliance limit already exists",
                    description = "The request tried to create a duplicate or conflicting gambling limit.",
                    value = ComplianceLimitExistsException.EXAMPLE
                )
            )
        )
    })
    @PostMapping
    ResponseEntity<GamblingLimitDto> createComplianceLimit(
        @Parameter(description = "Player profile ID", example = "123")
        @PathVariable Long playerId,

        @Valid @RequestBody CreateGamblingLimitDTO request
    );

    @Operation(
        summary = "Retrieve limits",
        description = "Returns all gambling limits for a player."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Compliance limits returned",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = GamblingLimitDto.class)),
                examples = @ExampleObject(
                    name = "compliance.limits",
                    summary = "Compliance limits",
                    description = "The gambling limits configured for the requested player.",
                    value = GamblingLimitDto.LIST_EXAMPLE
                )
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
        )
    })
    @GetMapping
    ResponseEntity<List<GamblingLimitDto>> getComplianceLimits(
        @Parameter(description = "Player profile ID", example = "123")
        @PathVariable Long playerId
    );

    @Operation(
        summary = "Modify limit",
        description = "Partially updates a gambling limit. Provided fields are modified."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Compliance limit updated",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = GamblingLimitDto.class),
                examples = @ExampleObject(
                    name = "compliance.limit.updated",
                    summary = "Updated compliance limit",
                    description = "Returned after successfully modifying a gambling limit.",
                    value = GamblingLimitDto.EXAMPLE
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
            description = "Compliance profile or limit missing",
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
                        name = "compliance.limit.missing",
                        summary = "Compliance limit missing",
                        description = "The requested gambling limit does not exist.",
                        value = ComplianceLimitMissingException.EXAMPLE
                    )
                }
            )
        )
    })
    @PatchMapping("/{limitId}")
    ResponseEntity<GamblingLimitDto> modifyComplianceLimit(
        @Parameter(description = "Player profile ID", example = "123")
        @PathVariable Long playerId,

        @Parameter(description = "Compliance limit ID", example = "456")
        @PathVariable Long limitId,

        @Valid @RequestBody ModifyGamblingLimitDTO request
    );
}