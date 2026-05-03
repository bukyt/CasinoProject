package com.casino.service;

import com.casino.dto.ComplianceLimitDto;
import com.casino.dto.CreateComplianceLimitDTO;
import com.casino.dto.ModifyComplianceLimitDTO;
import com.casino.exceptions.limit.ComplianceLimitExistsException;
import com.casino.exceptions.limit.ComplianceLimitMissingException;
import com.casino.exceptions.limit.InvalidComplianceLimitException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.ComplianceProfile;
import com.casino.model.GamblingLimit;
import com.casino.model.GamblingLimitPeriod;
import com.casino.model.GamblingLimitType;
import com.casino.repository.ComplianceProfileRepository;
import com.casino.repository.GamblingLimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LimitService {

    private final GamblingLimitRepository gamblingLimitRepository;
    private final ComplianceProfileRepository complianceProfileRepository;

    @Transactional
    public ComplianceLimitDto createComplianceLimit(
            Long playerId,
            CreateComplianceLimitDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        validateDateRange(request.startDate(), request.endDate());

        ensureNoDuplicateActiveLimit(
                playerId,
                profile.getComplianceId(),
                request.type(),
                request.period(),
                null
        );

        GamblingLimit limit = new GamblingLimit();
        limit.setComplianceId(profile.getComplianceId());
        limit.setType(request.type());
        limit.setAmount(request.amount());
        limit.setPeriod(request.period());
        limit.setCreatedDate(OffsetDateTime.now());
        limit.setStartDate(request.startDate());
        limit.setEndDate(request.endDate());
        limit.setRevokedDate(null);

        GamblingLimit savedLimit = gamblingLimitRepository.save(limit);

        return toDto(savedLimit);
    }

    @Transactional(readOnly = true)
    public List<ComplianceLimitDto> getComplianceLimits(Long playerId) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        return gamblingLimitRepository
                .findByComplianceIdOrderByCreatedDateDesc(profile.getComplianceId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ComplianceLimitDto modifyComplianceLimit(
            Long playerId,
            Long limitId,
            ModifyComplianceLimitDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        GamblingLimit limit = gamblingLimitRepository
                .findByLimitIdAndComplianceId(limitId, profile.getComplianceId())
                .orElseThrow(() -> new ComplianceLimitMissingException(limitId));

        GamblingLimitType newType = request.type() != null
                ? request.type()
                : limit.getType();

        GamblingLimitPeriod newPeriod = request.period() != null
                ? request.period()
                : limit.getPeriod();

        OffsetDateTime newStartDate = request.startDate() != null
                ? request.startDate()
                : limit.getStartDate();

        OffsetDateTime newEndDate = request.endDate() != null
                ? request.endDate()
                : limit.getEndDate();

        validateDateRange(newStartDate, newEndDate);

        ensureNoDuplicateActiveLimit(
                playerId,
                profile.getComplianceId(),
                newType,
                newPeriod,
                limitId
        );

        if (request.type() != null) {
            limit.setType(request.type());
        }

        if (request.amount() != null) {
            limit.setAmount(request.amount());
        }

        if (request.period() != null) {
            limit.setPeriod(request.period());
        }

        if (request.startDate() != null) {
            limit.setStartDate(request.startDate());
        }

        if (request.endDate() != null) {
            limit.setEndDate(request.endDate());
        }

        if (request.revokedDate() != null) {
            limit.setRevokedDate(request.revokedDate());
        }

        GamblingLimit savedLimit = gamblingLimitRepository.save(limit);

        return toDto(savedLimit);
    }

    private ComplianceProfile getComplianceProfileOrThrow(Long playerId) {
        return complianceProfileRepository
                .findByPlayerId(playerId)
                .orElseThrow(() -> new ComplianceProfileMissingException(playerId));
    }

    private void ensureNoDuplicateActiveLimit(
            Long playerId,
            Long complianceId,
            GamblingLimitType type,
            GamblingLimitPeriod period,
            Long excludedLimitId
    ) {
        OffsetDateTime now = OffsetDateTime.now();

        boolean duplicateExists = gamblingLimitRepository
                .findByComplianceIdAndTypeAndPeriod(complianceId, type, period)
                .stream()
                .anyMatch(limit ->
                        !Objects.equals(limit.getLimitId(), excludedLimitId)
                                && isActiveOrScheduled(limit, now)
                );

        if (duplicateExists) {
            throw new ComplianceLimitExistsException(playerId, type, period);
        }
    }

    private boolean isActiveOrScheduled(GamblingLimit limit, OffsetDateTime now) {
        boolean notRevoked = limit.getRevokedDate() == null;
        boolean notExpired = limit.getEndDate() == null || !limit.getEndDate().isBefore(now);

        return notRevoked && notExpired;
    }

    private void validateDateRange(
            OffsetDateTime startDate,
            OffsetDateTime endDate
    ) {
        if (startDate == null) {
            throw new InvalidComplianceLimitException("Limit start date is required.");
        }

        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new InvalidComplianceLimitException("Limit end date must be after start date.");
        }
    }

    private ComplianceLimitDto toDto(GamblingLimit limit) {
        return new ComplianceLimitDto(
                limit.getLimitId(),
                limit.getComplianceId(),
                limit.getType(),
                limit.getAmount(),
                limit.getPeriod(),
                limit.getCreatedDate(),
                limit.getStartDate(),
                limit.getEndDate(),
                limit.getRevokedDate()
        );
    }
}