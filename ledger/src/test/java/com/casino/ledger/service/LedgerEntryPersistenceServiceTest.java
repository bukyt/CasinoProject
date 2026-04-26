package com.casino.ledger.service;

import com.casino.ledger.controller.PlayerBalanceResponse;
import com.casino.ledger.controller.PlayerLedgerStatsResponse;
import com.casino.ledger.model.LedgerEntry;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.repository.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LedgerEntryPersistenceServiceTest {

    private LedgerEntryRepository ledgerEntryRepository;
    private LedgerEntryPersistenceService ledgerEntryPersistenceService;

    @BeforeEach
    void setUp() {
        ledgerEntryRepository = mock(LedgerEntryRepository.class);
        ledgerEntryPersistenceService = new LedgerEntryPersistenceService(ledgerEntryRepository);
    }

    @Test
    void getPlayerStatsReturnsAggregatesByEntryType() {
        when(ledgerEntryRepository.findByPlayerProfileIdOrderByCreatedDateDescEntryIdDesc(42))
                .thenReturn(List.of(
                        ledgerEntry(5, 42, LedgerEntryType.PLAYER_WIN, "30.00"),
                        ledgerEntry(4, 42, LedgerEntryType.PLAYER_LOSS, "10.00"),
                        ledgerEntry(3, 42, LedgerEntryType.PLAYER_BET, "50.00"),
                        ledgerEntry(2, 42, LedgerEntryType.PLAYER_WITHDRAW_FUNDS, "20.00"),
                        ledgerEntry(1, 42, LedgerEntryType.PLAYER_DEPOSIT_FUNDS, "100.00")
                ));

        PlayerLedgerStatsResponse stats = ledgerEntryPersistenceService.getPlayerStats(42);

        assertEquals(42, stats.playerProfileId());
        assertEquals(new BigDecimal("100.00"), stats.totalDeposits());
        assertEquals(new BigDecimal("20.00"), stats.totalWithdrawals());
        assertEquals(new BigDecimal("50.00"), stats.totalBets());
        assertEquals(new BigDecimal("30.00"), stats.totalWins());
        assertEquals(new BigDecimal("10.00"), stats.totalLosses());
    }

    @Test
    void getPlayerBalanceReturnsNetBalanceFromAggregates() {
        when(ledgerEntryRepository.findByPlayerProfileIdOrderByCreatedDateDescEntryIdDesc(42))
                .thenReturn(List.of(
                        ledgerEntry(5, 42, LedgerEntryType.PLAYER_WIN, "30.00"),
                        ledgerEntry(4, 42, LedgerEntryType.PLAYER_LOSS, "10.00"),
                        ledgerEntry(3, 42, LedgerEntryType.PLAYER_BET, "50.00"),
                        ledgerEntry(2, 42, LedgerEntryType.PLAYER_WITHDRAW_FUNDS, "20.00"),
                        ledgerEntry(1, 42, LedgerEntryType.PLAYER_DEPOSIT_FUNDS, "100.00")
                ));

        PlayerBalanceResponse balance = ledgerEntryPersistenceService.getPlayerBalance(42);

        assertEquals(42, balance.playerProfileId());
        assertEquals(new BigDecimal("50.00"), balance.balance());
    }

    private LedgerEntry ledgerEntry(Integer id, Integer playerProfileId, LedgerEntryType type, String amount) {
        return LedgerEntry.builder()
                .entryId(id)
                .playerProfileId(playerProfileId)
                .type(type)
                .amount(new BigDecimal(amount))
                .createdDate(Date.valueOf(LocalDate.of(2026, 4, 26)))
                .build();
    }
}
