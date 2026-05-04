package com.casino.ledger.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "ledgerentries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    Integer entryId;

    @NonNull
    @Column(name = "player_profile_id")
    Integer playerProfileId;

    @NonNull
    @Convert(converter = LedgerEntryType.LedgerEntryTypeConverter.class)
    LedgerEntryType type;

    @NonNull
    @Column(precision = 19, scale = 2)
    BigDecimal amount;

    @NonNull
    @Column(name = "created_date")
    Date createdDate;
}
