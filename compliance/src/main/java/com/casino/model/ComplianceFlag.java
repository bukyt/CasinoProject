package com.casino.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@NoArgsConstructor
public class ComplianceFlag {
    @Id
    private Long flagId;
    private Long complianceId;
    private ComplianceFlagType type;
    private ComplianceFlagSeverity severity;
    private OffsetDateTime createdDate;
    private OffsetDateTime resolvedDate;
}
