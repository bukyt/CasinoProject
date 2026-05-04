package com.casino.ledger.repository;

import com.casino.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Integer> {
    List<LedgerEntry> findByPlayerProfileIdOrderByCreatedDateDescEntryIdDesc(Integer playerProfileId);
}
