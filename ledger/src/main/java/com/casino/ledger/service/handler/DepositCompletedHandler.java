package com.casino.ledger.service.handler;

import com.casino.event.DepositCompleted;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepositCompletedHandler implements PlayerEventHandler<DepositCompleted> {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public DepositCompletedHandler(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @Override
    public Class<DepositCompleted> eventType() {
        return DepositCompleted.class;
    }

    @Override
    public void handle(DepositCompleted event) {
        ledgerEntryPersistenceService.persist(event, LedgerEntryType.PLAYER_DEPOSIT_FUNDS);
        log.info("Persisted ledger entry for deposit event: {}", event);
    }
}
