package com.casino.ledger.controller;

import com.casino.ledger.model.LedgerEntry;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.service.LedgerEntryNotFoundException;
import com.casino.ledger.service.LedgerEntryPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LedgerControllerTest {

    private LedgerEntryPersistenceService ledgerEntryPersistenceService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ledgerEntryPersistenceService = mock(LedgerEntryPersistenceService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LedgerController(ledgerEntryPersistenceService))
                .build();
    }

    @Test
    void getLedgerEntryReturnsEntry() throws Exception {
        when(ledgerEntryPersistenceService.getById(11))
                .thenReturn(ledgerEntry(11, 456, LedgerEntryType.PLAYER_WIN, "99.99", LocalDate.of(2026, 4, 25)));

        mockMvc.perform(get("/ledger/entries/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.playerProfileId").value(456))
                .andExpect(jsonPath("$.type").value("PLAYER_WIN"))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.createdDate").value("2026-04-25"));
    }

    @Test
    void getLedgerEntryReturnsNotFoundWhenMissing() throws Exception {
        when(ledgerEntryPersistenceService.getById(404))
                .thenThrow(new LedgerEntryNotFoundException(404));

        mockMvc.perform(get("/ledger/entries/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ledger entry not found: 404"));
    }

    @Test
    void getPlayerTransactionHistoryReturnsEntries() throws Exception {
        when(ledgerEntryPersistenceService.getPlayerHistory(999))
                .thenReturn(List.of(
                        ledgerEntry(3, 999, LedgerEntryType.PLAYER_WIN, "25.00", LocalDate.of(2026, 4, 26)),
                        ledgerEntry(2, 999, LedgerEntryType.PLAYER_BET, "10.00", LocalDate.of(2026, 4, 25))
                ));

        mockMvc.perform(get("/ledger/player/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].type").value("PLAYER_WIN"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].type").value("PLAYER_BET"));
    }

    @Test
    void getPlayerBalanceReturnsComputedBalance() throws Exception {
        when(ledgerEntryPersistenceService.getPlayerBalance(123))
                .thenReturn(new PlayerBalanceResponse(123, new BigDecimal("97.50")));

        mockMvc.perform(get("/ledger/player/123/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerProfileId").value(123))
                .andExpect(jsonPath("$.balance").value(97.50));
    }

    @Test
    void getPlayerStatsReturnsAggregates() throws Exception {
        when(ledgerEntryPersistenceService.getPlayerStats(123))
                .thenReturn(new PlayerLedgerStatsResponse(
                        123,
                        new BigDecimal("200.00"),
                        new BigDecimal("40.00"),
                        new BigDecimal("30.00"),
                        new BigDecimal("15.00"),
                        new BigDecimal("7.50")
                ));

        mockMvc.perform(get("/ledger/player/123/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerProfileId").value(123))
                .andExpect(jsonPath("$.totalDeposits").value(200.00))
                .andExpect(jsonPath("$.totalWithdrawals").value(40.00))
                .andExpect(jsonPath("$.totalBets").value(30.00))
                .andExpect(jsonPath("$.totalWins").value(15.00))
                .andExpect(jsonPath("$.totalLosses").value(7.50));
    }

    private LedgerEntry ledgerEntry(
            Integer id,
            Integer playerProfileId,
            LedgerEntryType type,
            String amount,
            LocalDate createdDate
    ) {
        return LedgerEntry.builder()
                .entryId(id)
                .playerProfileId(playerProfileId)
                .type(type)
                .amount(new BigDecimal(amount))
                .createdDate(Date.valueOf(createdDate))
                .build();
    }
}
