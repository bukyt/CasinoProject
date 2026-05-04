package com.casino.ledger.controller;

import com.casino.ledger.service.LedgerEntryNotFoundException;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public LedgerController(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @GetMapping("/entries/{id:\\d+}")
    public LedgerEntryResponse getLedgerEntry(@PathVariable Integer id) {
        return LedgerEntryResponse.from(ledgerEntryPersistenceService.getById(id));
    }

    @GetMapping("/player/{playerProfileId:\\d+}")
    public List<LedgerEntryResponse> getPlayerTransactionHistory(@PathVariable Integer playerProfileId) {
        return ledgerEntryPersistenceService.getPlayerHistory(playerProfileId)
                .stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }

    @GetMapping("/player/{playerProfileId:\\d+}/balance")
    public PlayerBalanceResponse getPlayerBalance(@PathVariable Integer playerProfileId) {
        return ledgerEntryPersistenceService.getPlayerBalance(playerProfileId);
    }

    @GetMapping("/player/{playerProfileId:\\d+}/stats")
    public PlayerLedgerStatsResponse getPlayerStats(@PathVariable Integer playerProfileId) {
        return ledgerEntryPersistenceService.getPlayerStats(playerProfileId);
    }

    @ExceptionHandler(LedgerEntryNotFoundException.class)
    public ProblemDetail handleLedgerEntryNotFound(LedgerEntryNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(org.springframework.http.HttpStatus.NOT_FOUND, exception.getMessage());
    }
}
