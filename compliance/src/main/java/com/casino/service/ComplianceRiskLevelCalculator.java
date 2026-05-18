package com.casino.service;

import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ComplianceRiskLevelCalculator {

    private final ComplianceFlagRepository complianceFlagRepository;

    public ComplianceProfileRiskLevel calculateFromUnresolvedFlags(Long complianceId) {
        List<ComplianceFlag> flags =
            complianceFlagRepository.findByComplianceProfile_ComplianceId(complianceId);

        boolean hasHigh = flags.stream()
            .anyMatch(flag ->
                isUnresolved(flag)
                    && flag.getSeverity() == ComplianceFlagSeverity.HIGH
            );

        if (hasHigh) {
            return ComplianceProfileRiskLevel.HIGH;
        }

        boolean hasMedium = flags.stream()
            .anyMatch(flag ->
                isUnresolved(flag)
                    && flag.getSeverity() == ComplianceFlagSeverity.MEDIUM
            );

        if (hasMedium) {
            return ComplianceProfileRiskLevel.MEDIUM;
        }

        boolean hasLow = flags.stream()
            .anyMatch(flag ->
                isUnresolved(flag)
                    && flag.getSeverity() == ComplianceFlagSeverity.LOW
            );

        if (hasLow) {
            return ComplianceProfileRiskLevel.LOW;
        }

        return ComplianceProfileRiskLevel.LOW;
    }

    private boolean isUnresolved(ComplianceFlag flag) {
        return !isResolvedState(
            flag.getSeverity(),
            flag.getResolvedDate()
        );
    }

    private boolean isResolvedState(
        ComplianceFlagSeverity severity,
        OffsetDateTime resolvedDate
    ) {
        return resolvedDate != null && isResolvedSeverity(severity);
    }

    private boolean isResolvedSeverity(ComplianceFlagSeverity severity) {
        return severity == ComplianceFlagSeverity.RESOLVED
            || severity == ComplianceFlagSeverity.RESOLVED_ADMIN;
    }
}