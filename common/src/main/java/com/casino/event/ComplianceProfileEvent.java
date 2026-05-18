package com.casino.event;


import java.time.OffsetDateTime;

public record ComplianceProfileEvent(
    Long playerProfileId,
    boolean selfExcluded,
    ComplianceRiskLevel riskLevel,
    OffsetDateTime occurredAt
) {
}