package com.casino.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComplianceLimitChanged(
        UUID eventId,
        int schemaVersion,
        Long playerProfileId,
        Long complianceId,
        Long limitId,
        GamblingLimitEventAction action,
        String type,
        Integer amount,
        String period,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime revokedDate,
        OffsetDateTime occurredAt,
        String source
) {}
