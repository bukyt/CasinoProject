package com.casino.ledger.service.handler;

import com.casino.event.WithdrawalProcessed;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WithdrawalProcessedHandler implements PlayerEventHandler<WithdrawalProcessed> {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public WithdrawalProcessedHandler(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @Override
    public Class<WithdrawalProcessed> eventType() {
        return WithdrawalProcessed.class;
    }

    @Override
    public void handle(WithdrawalProcessed event) {
        ledgerEntryPersistenceService.persist(event, LedgerEntryType.PLAYER_WITHDRAW_FUNDS);
        log.info("Persisted ledger entry for withdraw event: {}", event);
    }
}
