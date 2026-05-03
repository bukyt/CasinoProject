package com.casino.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compliance_profile")
public class ComplianceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compliance_id")
    private Long complianceId;

    @Column(name = "player_profile_id")
    @NotNull
    private Long playerProfileId;

    @Column(name = "is_age_verified")
    private boolean ageVerified;

    @Column(name = "is_self_excluded")
    private boolean selfExcluded;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private ComplianceProfileRiskLevel riskLevel;

    @Column(name = "last_review_data")
    private OffsetDateTime lastReviewDate;
}