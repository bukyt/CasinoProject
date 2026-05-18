package com.casino.event;

public record ComplianceStatusSnapshot(
        boolean ageVerified,
        boolean selfExcluded,
        ComplianceRiskLevel riskLevel,
        boolean gamblingAllowed
) {}
