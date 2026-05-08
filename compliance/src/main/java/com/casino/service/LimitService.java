package com.casino.service;

import com.casino.dto.limit.CreateGamblingLimitDTO;
import com.casino.dto.limit.GamblingLimitDto;
import com.casino.dto.limit.ModifyGamblingLimitDTO;
import com.casino.exceptions.limit.ComplianceLimitMissingException;
import com.casino.exceptions.limit.InvalidComplianceLimitException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.limit.GamblingLimit;
import com.casino.model.limit.GamblingLimitPeriod;
import com.casino.model.limit.GamblingLimitType;
import com.casino.model.profile.ComplianceProfile;
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
    public GamblingLimitDto createComplianceLimit(
        Long playerId,
        CreateGamblingLimitDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        validateCreateRequest(request);

        OffsetDateTime now = OffsetDateTime.now();

        revokeCurrentlyActiveLimitsOfSameType(
            profile.getComplianceId(),
            request.type(),
            now,
            null
        );

        GamblingLimit limit = new GamblingLimit();
        limit.setComplianceProfile(profile);
        limit.setType(request.type());
        limit.setAmount(request.amount());
        limit.setPeriod(request.period());
        limit.setCreatedDate(now);
        limit.setStartDate(request.startDate());
        limit.setEndDate(request.endDate());
        limit.setRevokedDate(null);

        profile.setLastReviewDate(now);

        GamblingLimit savedLimit = gamblingLimitRepository.save(limit);
        complianceProfileRepository.save(profile);

        return toDto(savedLimit);
    }

    @Transactional(readOnly = true)
    public List<GamblingLimitDto> getComplianceLimits(Long playerId) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        return gamblingLimitRepository
            .findByComplianceProfile_ComplianceIdOrderByCreatedDateDesc(profile.getComplianceId())
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public GamblingLimitDto modifyComplianceLimit(
        Long playerId,
        Long limitId,
        ModifyGamblingLimitDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        GamblingLimit limit = gamblingLimitRepository
            .findByLimitIdAndComplianceProfile_ComplianceId(
                limitId,
                profile.getComplianceId()
            )
            .orElseThrow(() -> new ComplianceLimitMissingException(limitId));

        GamblingLimitType newType = request.type() != null
            ? request.type()
            : limit.getType();

        GamblingLimitPeriod newPeriod = request.period() != null
            ? request.period()
            : limit.getPeriod();

        Integer newAmount = request.amount() != null
            ? request.amount()
            : limit.getAmount();

        OffsetDateTime newStartDate = request.startDate() != null
            ? request.startDate()
            : limit.getStartDate();

        OffsetDateTime newEndDate = request.endDate() != null
            ? request.endDate()
            : limit.getEndDate();

        OffsetDateTime newRevokedDate = request.revokedDate() != null
            ? request.revokedDate()
            : limit.getRevokedDate();

        validateAmount(newAmount);
        validateDateRange(newStartDate, newEndDate);

        OffsetDateTime now = OffsetDateTime.now();

        if (isCurrentlyActive(newStartDate, newEndDate, newRevokedDate, now)) {
            revokeCurrentlyActiveLimitsOfSameType(
                profile.getComplianceId(),
                newType,
                now,
                limitId
            );
        }

        limit.setType(newType);
        limit.setAmount(newAmount);
        limit.setPeriod(newPeriod);
        limit.setStartDate(newStartDate);
        limit.setEndDate(newEndDate);
        limit.setRevokedDate(newRevokedDate);

        profile.setLastReviewDate(now);

        GamblingLimit savedLimit = gamblingLimitRepository.save(limit);
        complianceProfileRepository.save(profile);

        return toDto(savedLimit);
    }

    private ComplianceProfile getComplianceProfileOrThrow(Long playerId) {
        return complianceProfileRepository
            .findFirstByPlayerProfileId(playerId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerId));
    }

    private void revokeCurrentlyActiveLimitsOfSameType(
        Long complianceId,
        GamblingLimitType type,
        OffsetDateTime now,
        Long excludedLimitId
    ) {
        List<GamblingLimit> activeLimitsToRevoke = gamblingLimitRepository
            .findByComplianceProfile_ComplianceIdAndType(complianceId, type)
            .stream()
            .filter(limit -> !Objects.equals(limit.getLimitId(), excludedLimitId))
            .filter(limit -> isCurrentlyActive(limit, now))
            .toList();

        activeLimitsToRevoke.forEach(limit -> limit.setRevokedDate(now));

        gamblingLimitRepository.saveAll(activeLimitsToRevoke);
    }

    private boolean isCurrentlyActive(GamblingLimit limit, OffsetDateTime now) {
        return isCurrentlyActive(
            limit.getStartDate(),
            limit.getEndDate(),
            limit.getRevokedDate(),
            now
        );
    }

    private boolean isCurrentlyActive(
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime revokedDate,
        OffsetDateTime now
    ) {
        boolean started = startDate != null && !startDate.isAfter(now);
        boolean notExpired = endDate == null || endDate.isAfter(now);
        boolean notRevoked = revokedDate == null;

        return started && notExpired && notRevoked;
    }

    private void validateCreateRequest(CreateGamblingLimitDTO request) {
        if (request.type() == null) {
            throw new InvalidComplianceLimitException("Limit type is required.");
        }

        if (request.period() == null) {
            throw new InvalidComplianceLimitException("Limit period is required.");
        }

        validateAmount(request.amount());
        validateDateRange(request.startDate(), request.endDate());
    }

    private void validateAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new InvalidComplianceLimitException("Limit amount must be greater than zero.");
        }
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

    private GamblingLimitDto toDto(GamblingLimit limit) {
        return new GamblingLimitDto(
            limit.getLimitId(),
            limit.getComplianceProfile().getComplianceId(),
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