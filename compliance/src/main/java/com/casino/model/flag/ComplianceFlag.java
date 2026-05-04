package com.casino.model.flag;

import com.casino.model.profile.ComplianceProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compliance_flags")
public class ComplianceFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flag_id", nullable = false, updatable = false)
    private Long flagId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compliance_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ComplianceProfile complianceProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", nullable = false, length = 50)
    private ComplianceFlagType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private ComplianceFlagSeverity severity;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "resolved_date")
    private OffsetDateTime resolvedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = OffsetDateTime.now();
        }
    }
}