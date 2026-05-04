package com.casino.ledger.controller;

import com.casino.ledger.service.LedgerEntryNotFoundException;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ledger")
@Tag(name = "Ledger")
public class LedgerController {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public LedgerController(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @GetMapping("/entries/{id:\\d+}")
    @Operation(summary = "Get a ledger entry by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ledger entry found",
                    content = @Content(schema = @Schema(implementation = LedgerEntryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ledger entry not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public LedgerEntryResponse getLedgerEntry(
            @Parameter(description = "Ledger entry identifier", example = "11")
            @PathVariable Integer id
    ) {
        return LedgerEntryResponse.from(ledgerEntryPersistenceService.getById(id));
    }

    @GetMapping("/player/{playerProfileId:\\d+}")
    @Operation(summary = "Get a player's transaction history")
    @ApiResponse(responseCode = "200", description = "Ledger history returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LedgerEntryResponse.class))))
    public List<LedgerEntryResponse> getPlayerTransactionHistory(
            @Parameter(description = "Player profile identifier", example = "999")
            @PathVariable Integer playerProfileId
    ) {
        return ledgerEntryPersistenceService.getPlayerHistory(playerProfileId)
                .stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }

    @GetMapping("/player/{playerProfileId:\\d+}/balance")
    @Operation(summary = "Get the current player balance")
    @ApiResponse(responseCode = "200", description = "Player balance returned",
            content = @Content(schema = @Schema(implementation = PlayerBalanceResponse.class)))
    public PlayerBalanceResponse getPlayerBalance(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId
    ) {
        return ledgerEntryPersistenceService.getPlayerBalance(playerProfileId);
    }

    @GetMapping("/player/{playerProfileId:\\d+}/stats")
    @Operation(summary = "Get aggregated player ledger stats")
    @ApiResponse(responseCode = "200", description = "Player ledger stats returned",
            content = @Content(schema = @Schema(implementation = PlayerLedgerStatsResponse.class)))
    public PlayerLedgerStatsResponse getPlayerStats(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId
    ) {
        return ledgerEntryPersistenceService.getPlayerStats(playerProfileId);
    }

    @ExceptionHandler(LedgerEntryNotFoundException.class)
    @ApiResponse(responseCode = "404", description = "Ledger entry not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ProblemDetail handleLedgerEntryNotFound(LedgerEntryNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
