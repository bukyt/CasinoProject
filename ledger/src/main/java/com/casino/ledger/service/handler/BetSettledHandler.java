package com.casino.ledger.service.handler;

import com.casino.event.BetSettled;
import com.casino.event.GameEndingType;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BetSettledHandler implements PlayerEventHandler<BetSettled> {

    private final LedgerEntryPersistenceService ledgerEntryPersistenceService;

    public BetSettledHandler(LedgerEntryPersistenceService ledgerEntryPersistenceService) {
        this.ledgerEntryPersistenceService = ledgerEntryPersistenceService;
    }

    @Override
    public Class<BetSettled> eventType() {
        return BetSettled.class;
    }

    @Override
    public void handle(BetSettled event) {
        ledgerEntryPersistenceService.persist(event, resolveEntryType(event));
        log.info("Persisted ledger entry for game-ended event: {}", event);
    }

    private LedgerEntryType resolveEntryType(BetSettled event) {
        if (event.getGameEndingType() == GameEndingType.WIN) {
            return LedgerEntryType.PLAYER_WIN;
        }
        return LedgerEntryType.PLAYER_LOSS;
    }
}
