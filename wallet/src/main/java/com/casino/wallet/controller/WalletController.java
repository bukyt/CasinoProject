package com.casino.wallet.controller;

import com.casino.wallet.dto.WalletAmountRequest;
import com.casino.wallet.dto.WalletResponse;
import com.casino.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@Tag(name = "Wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{playerProfileId:\\d+}")
    @Operation(summary = "Get a player's wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet found",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public WalletResponse getWallet(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId
    ) {
        return walletService.getWallet(playerProfileId);
    }

    @PostMapping("/create/{playerProfileId:\\d+}")
    @Operation(summary = "Create a player wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet created",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "400", description = "Wallet already exists")
    })
    public WalletResponse createWallet(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId
    ) {
        return walletService.createWallet(playerProfileId);
    }

    @PostMapping("/debit/{playerProfileId:\\d+}")
    @Operation(summary = "Debit funds into a wallet",
            description = "Increases the player's available wallet balance by the requested amount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet debited",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public WalletResponse debit(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        return walletService.debit(playerProfileId, request.amount());
    }

    @PostMapping("/credit/{playerProfileId:\\d+}")
    @Operation(summary = "Credit funds from a wallet",
            description = "Decreases the player's available wallet balance by the requested amount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet credited",
                    content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public WalletResponse credit(
            @Parameter(description = "Player profile identifier", example = "123")
            @PathVariable Integer playerProfileId,
            @Valid @RequestBody WalletAmountRequest request
    ) {
        return walletService.credit(playerProfileId, request.amount());
    }
}
