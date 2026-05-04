package com.casino.model.limit;

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
@Table(name = "gambling_limits")
public class GamblingLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_id", nullable = false, updatable = false)
    private Long limitId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compliance_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ComplianceProfile complianceProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type", nullable = false, length = 50)
    private GamblingLimitType type;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 50)
    private GamblingLimitPeriod period;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "revoked_date")
    private OffsetDateTime revokedDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = OffsetDateTime.now();
        }
    }
}