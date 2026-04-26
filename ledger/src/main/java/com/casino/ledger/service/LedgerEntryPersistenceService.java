package com.casino.ledger.service;

import com.casino.event.AbstractPlayerFinancialEvent;
import com.casino.ledger.controller.PlayerBalanceResponse;
import com.casino.ledger.controller.PlayerLedgerStatsResponse;
import com.casino.ledger.model.LedgerEntry;
import com.casino.ledger.model.LedgerEntryType;
import com.casino.ledger.repository.LedgerEntryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Service
public class LedgerEntryPersistenceService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryPersistenceService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public LedgerEntry persist(AbstractPlayerFinancialEvent event, LedgerEntryType type) {
        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .playerProfileId(event.getPlayerProfileId())
                .type(type)
                .amount(event.getAmount())
                .createdDate(new Date(System.currentTimeMillis()))
                .build();
        return ledgerEntryRepository.save(ledgerEntry);
    }

    public LedgerEntry getById(Integer id) {
        return ledgerEntryRepository.findById(id)
                .orElseThrow(() -> new LedgerEntryNotFoundException(id));
    }

    public List<LedgerEntry> getPlayerHistory(Integer playerProfileId) {
        return ledgerEntryRepository.findByPlayerProfileIdOrderByCreatedDateDescEntryIdDesc(playerProfileId);
    }

    public PlayerBalanceResponse getPlayerBalance(Integer playerProfileId) {
        PlayerLedgerStatsResponse stats = getPlayerStats(playerProfileId);
        BigDecimal balance = stats.totalDeposits()
                .add(stats.totalWins())
                .subtract(stats.totalWithdrawals())
                .subtract(stats.totalBets())
                .subtract(stats.totalLosses());

        return new PlayerBalanceResponse(playerProfileId, balance);
    }

    public PlayerLedgerStatsResponse getPlayerStats(Integer playerProfileId) {
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalBets = BigDecimal.ZERO;
        BigDecimal totalWins = BigDecimal.ZERO;
        BigDecimal totalLosses = BigDecimal.ZERO;

        for (LedgerEntry ledgerEntry : getPlayerHistory(playerProfileId)) {
            switch (ledgerEntry.getType()) {
                case PLAYER_DEPOSIT_FUNDS -> totalDeposits = totalDeposits.add(ledgerEntry.getAmount());
                case PLAYER_WITHDRAW_FUNDS -> totalWithdrawals = totalWithdrawals.add(ledgerEntry.getAmount());
                case PLAYER_BET -> totalBets = totalBets.add(ledgerEntry.getAmount());
                case PLAYER_WIN -> totalWins = totalWins.add(ledgerEntry.getAmount());
                case PLAYER_LOSS -> totalLosses = totalLosses.add(ledgerEntry.getAmount());
            }
        }

        return new PlayerLedgerStatsResponse(
                playerProfileId,
                totalDeposits,
                totalWithdrawals,
                totalBets,
                totalWins,
                totalLosses
        );
    }
}
