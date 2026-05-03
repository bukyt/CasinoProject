package com.casino.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Entity
@AllArgsConstructor
public class ComplianceFlag {
    @Id
    private Long flagId;
    private Long complianceId;
    private ComplianceFlagType type;
    private ComplianceFlagSeverity severity;
    private OffsetDateTime createdDate;
    private OffsetDateTime resolvedDate;
}
