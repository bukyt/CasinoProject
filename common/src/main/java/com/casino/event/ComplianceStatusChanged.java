package com.casino.event;


import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ComplianceStatusChanged(
        UUID eventId,
        int schemaVersion,
        Long playerProfileId,
        Long complianceId,
        OffsetDateTime occurredAt,
        String source,
        ComplianceStatusSnapshot previousStatus,
        ComplianceStatusSnapshot currentStatus,
        Set<ComplianceStatusChangeReason> reasons
) {}
