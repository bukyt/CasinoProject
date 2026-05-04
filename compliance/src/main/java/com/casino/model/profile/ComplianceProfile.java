package com.casino.model.profile;

import com.casino.model.flag.ComplianceFlag;
import com.casino.model.limit.GamblingLimit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compliance_profile")
public class ComplianceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compliance_id")
    private Long complianceId;

    @NotNull
    @Column(name = "player_profile_id", nullable = false)
    private Long playerProfileId;

    @Column(name = "is_age_verified")
    private boolean ageVerified;

    @Column(name = "is_self_excluded")
    private boolean selfExcluded;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private ComplianceProfileRiskLevel riskLevel;

    @Column(name = "last_review_date")
    private OffsetDateTime lastReviewDate;

    @OneToMany(
        mappedBy = "complianceProfile",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<GamblingLimit> limits = new ArrayList<>();

    @OneToMany(
        mappedBy = "complianceProfile",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ComplianceFlag> flags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        riskLevel = ComplianceProfileRiskLevel.UNASSESSED;
    }
}