package com.casino.ledger.service.handler;

import com.casino.event.BetSettled;
import com.casino.event.GameEndingType;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BetSettledHandlerTest {

    private LedgerEntryPersistenceService ledgerEntryPersistenceService;
    private BetSettledHandler betSettledHandler;

    @BeforeEach
    void setUp() {
        ledgerEntryPersistenceService = mock(LedgerEntryPersistenceService.class);
        betSettledHandler = new BetSettledHandler(ledgerEntryPersistenceService);
    }

    @Test
    void handlePersistsWinEntryForWinEvents() {
        BetSettled event = settledEvent(GameEndingType.WIN);

        betSettledHandler.handle(event);

        verify(ledgerEntryPersistenceService).persist(eq(event), eq(LedgerEntryType.PLAYER_WIN));
    }

    @Test
    void handlePersistsLossEntryForLoseEvents() {
        BetSettled event = settledEvent(GameEndingType.LOSE);

        betSettledHandler.handle(event);

        verify(ledgerEntryPersistenceService).persist(eq(event), eq(LedgerEntryType.PLAYER_LOSS));
    }

    private BetSettled settledEvent(GameEndingType gameEndingType) {
        BetSettled event = new BetSettled();
        event.setPlayerProfileId(42);
        event.setAmount(new BigDecimal("15.00"));
        event.setGameEndingType(gameEndingType);
        return event;
    }
}
