package com.casino.service;

import com.casino.dto.flag.ComplianceFlagDto;
import com.casino.dto.flag.CreateComplianceFlagDTO;
import com.casino.dto.flag.ModifyComplianceFlagDTO;
import com.casino.exceptions.flag.ComplianceFlagExistsException;
import com.casino.exceptions.flag.ComplianceFlagMissingException;
import com.casino.exceptions.flag.InvalidComplianceFlagException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import com.casino.repository.ComplianceProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FlagService {

    private final ComplianceFlagRepository complianceFlagRepository;
    private final ComplianceProfileRepository complianceProfileRepository;
    private final ComplianceEventProducer complianceEventProducer;
    private final ComplianceRiskLevelCalculator complianceRiskLevelCalculator;

    @Transactional
    public ComplianceFlagDto createComplianceFlag(
        Long playerId,
        CreateComplianceFlagDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        boolean previousSelfExcluded = profile.isSelfExcluded();
        ComplianceProfileRiskLevel previousRiskLevel = profile.getRiskLevel();

        validateCreateRequest(request);

        ensureNoDuplicateUnresolvedType(
            playerId,
            profile.getComplianceId(),
            request.type(),
            null
        );

        OffsetDateTime now = OffsetDateTime.now();

        ComplianceFlag flag = new ComplianceFlag();
        flag.setComplianceProfile(profile);
        flag.setType(request.type());
        flag.setSeverity(request.severity());
        flag.setCreatedDate(now);
        flag.setResolvedDate(null);

        ComplianceFlag savedFlag = complianceFlagRepository.saveAndFlush(flag);

        updateProfileAfterFlagChange(profile, now);
        ComplianceProfile savedProfile = complianceProfileRepository.save(profile);

        complianceEventProducer.publishStatusChangedIfChanged(
            savedProfile.getPlayerProfileId(),
            previousSelfExcluded,
            previousRiskLevel,
            savedProfile.isSelfExcluded(),
            savedProfile.getRiskLevel()
        );

        return toDto(savedFlag);
    }

    @Transactional
    public ComplianceFlagDto modifyComplianceFlag(
        Long playerId,
        Long flagId,
        ModifyComplianceFlagDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        boolean previousSelfExcluded = profile.isSelfExcluded();
        ComplianceProfileRiskLevel previousRiskLevel = profile.getRiskLevel();

        ComplianceFlag flag = complianceFlagRepository
            .findByFlagIdAndComplianceProfile_ComplianceId(
                flagId,
                profile.getComplianceId()
            )
            .orElseThrow(() -> new ComplianceFlagMissingException(flagId));

        OffsetDateTime now = OffsetDateTime.now();

        ComplianceFlagType newType = request.type() != null
            ? request.type()
            : flag.getType();

        ComplianceFlagSeverity newSeverity = request.severity() != null
            ? request.severity()
            : flag.getSeverity();

        OffsetDateTime newResolvedDate = request.resolvedDate() != null
            ? request.resolvedDate()
            : flag.getResolvedDate();

        if (isResolvedSeverity(newSeverity) && newResolvedDate == null) {
            newResolvedDate = now;
        }

        validateModifiedValues(
            flag.getCreatedDate(),
            newType,
            newSeverity,
            newResolvedDate
        );

        boolean finalStateIsResolved = isResolvedState(newSeverity, newResolvedDate);

        if (!finalStateIsResolved) {
            ensureNoDuplicateUnresolvedType(
                playerId,
                profile.getComplianceId(),
                newType,
                flagId
            );
        }

        flag.setType(newType);
        flag.setSeverity(newSeverity);
        flag.setResolvedDate(newResolvedDate);

        ComplianceFlag savedFlag = complianceFlagRepository.saveAndFlush(flag);

        updateProfileAfterFlagChange(profile, now);
        ComplianceProfile savedProfile = complianceProfileRepository.save(profile);

        complianceEventProducer.publishStatusChangedIfChanged(
            savedProfile.getPlayerProfileId(),
            previousSelfExcluded,
            previousRiskLevel,
            savedProfile.isSelfExcluded(),
            savedProfile.getRiskLevel()
        );

        return toDto(savedFlag);
    }

    private ComplianceProfile getComplianceProfileOrThrow(Long playerId) {
        return complianceProfileRepository
            .findFirstByPlayerProfileId(playerId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerId));
    }

    private void validateCreateRequest(CreateComplianceFlagDTO request) {
        if (request.type() == null) {
            throw new InvalidComplianceFlagException("Flag type is required.");
        }

        if (request.severity() == null) {
            throw new InvalidComplianceFlagException("Flag severity is required.");
        }

        if (isResolvedSeverity(request.severity())) {
            throw new InvalidComplianceFlagException(
                "A new flag cannot be created as resolved. Create the flag first, then resolve it using PATCH."
            );
        }
    }

    private void validateModifiedValues(
        OffsetDateTime createdDate,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity,
        OffsetDateTime resolvedDate
    ) {
        if (type == null) {
            throw new InvalidComplianceFlagException("Flag type is required.");
        }

        if (severity == null) {
            throw new InvalidComplianceFlagException("Flag severity is required.");
        }

        validateResolvedDate(createdDate, resolvedDate);
        validateResolvedState(severity, resolvedDate);
    }

    private void validateResolvedDate(
        OffsetDateTime createdDate,
        OffsetDateTime resolvedDate
    ) {
        if (resolvedDate != null && resolvedDate.isBefore(createdDate)) {
            throw new InvalidComplianceFlagException(
                "Resolved date cannot be before created date."
            );
        }
    }

    private void validateResolvedState(
        ComplianceFlagSeverity severity,
        OffsetDateTime resolvedDate
    ) {
        boolean hasResolvedDate = resolvedDate != null;
        boolean hasResolvedSeverity = isResolvedSeverity(severity);

        if (hasResolvedDate && !hasResolvedSeverity) {
            throw new InvalidComplianceFlagException(
                "A flag with a resolved date must have severity RESOLVED or RESOLVED_ADMIN."
            );
        }

        if (hasResolvedSeverity && !hasResolvedDate) {
            throw new InvalidComplianceFlagException(
                "A flag with severity RESOLVED or RESOLVED_ADMIN must have a resolved date."
            );
        }
    }

    private void ensureNoDuplicateUnresolvedType(
        Long playerId,
        Long complianceId,
        ComplianceFlagType type,
        Long excludedFlagId
    ) {
        List<ComplianceFlag> existingFlags = complianceFlagRepository
            .findByComplianceProfile_ComplianceIdAndType(
                complianceId,
                type
            );

        boolean duplicateExists = existingFlags.stream()
            .anyMatch(flag ->
                !Objects.equals(flag.getFlagId(), excludedFlagId)
                    && isUnresolved(flag)
            );

        if (duplicateExists) {
            throw new ComplianceFlagExistsException(playerId, type);
        }
    }

    private void updateProfileAfterFlagChange(
        ComplianceProfile profile,
        OffsetDateTime changedAt
    ) {
        profile.setLastReviewDate(changedAt);
        profile.setRiskLevel(
            calculateRiskLevelFromUnresolvedFlags(profile.getComplianceId())
        );
    }

    private ComplianceProfileRiskLevel calculateRiskLevelFromUnresolvedFlags(Long complianceId) {
        List<ComplianceFlag> flags = complianceFlagRepository
            .findByComplianceProfile_ComplianceId(complianceId);

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
            return ComplianceProfileRiskLevel.HIGH;
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

    private ComplianceFlagDto toDto(ComplianceFlag flag) {
        return new ComplianceFlagDto(
            flag.getFlagId(),
            flag.getComplianceProfile().getComplianceId(),
            flag.getType(),
            flag.getSeverity(),
            flag.getCreatedDate(),
            flag.getResolvedDate()
        );
    }
}