package com.casino.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComplianceFlagChanged(
        UUID eventId,
        int schemaVersion,
        Long playerProfileId,
        Long complianceId,
        Long flagId,
        ComplianceFlagEventAction action,
        String type,
        String previousType,
        String severity,
        String previousSeverity,
        OffsetDateTime createdDate,
        OffsetDateTime resolvedDate,
        OffsetDateTime occurredAt,
        String source
) {}
