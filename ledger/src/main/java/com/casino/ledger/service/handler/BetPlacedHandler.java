package com.casino.ledger.service.handler;

import com.casino.event.BetPlaced;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BetPlacedHandler implements PlayerEventHandler<BetPlaced> {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public BetPlacedHandler(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @Override
    public Class<BetPlaced> eventType() {
        return BetPlaced.class;
    }

    @Override
    public void handle(BetPlaced event) {
        ledgerEntryPersistenceService.persist(event, LedgerEntryType.PLAYER_BET);
        log.info("Persisted ledger entry for bet event: {}", event);
    }
}
