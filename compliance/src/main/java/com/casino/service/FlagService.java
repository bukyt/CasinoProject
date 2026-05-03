package com.casino.service;

import com.casino.dto.ComplianceFlagDto;
import com.casino.dto.CreateComplianceFlagDTO;
import com.casino.dto.ModifyComplianceFlagDTO;
import com.casino.exceptions.flag.ComplianceFlagExistsException;
import com.casino.exceptions.flag.ComplianceFlagMissingException;
import com.casino.exceptions.flag.InvalidComplianceFlagException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.ComplianceFlag;
import com.casino.model.ComplianceFlagSeverity;
import com.casino.model.ComplianceFlagType;
import com.casino.model.ComplianceProfile;
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

    @Transactional
    public ComplianceFlagDto createComplianceFlag(
            Long playerId,
            CreateComplianceFlagDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        ensureNoDuplicateActiveFlag(
                playerId,
                profile.getComplianceId(),
                request.type(),
                request.severity(),
                null
        );

        ComplianceFlag flag = new ComplianceFlag();
        flag.setComplianceId(profile.getComplianceId());
        flag.setType(request.type());
        flag.setSeverity(request.severity());
        flag.setCreatedDate(OffsetDateTime.now());
        flag.setResolvedDate(null);

        ComplianceFlag savedFlag = complianceFlagRepository.save(flag);

        return toDto(savedFlag);
    }

    @Transactional
    public ComplianceFlagDto modifyComplianceFlag(
            Long playerId,
            Long flagId,
            ModifyComplianceFlagDTO request
    ) {
        ComplianceProfile profile = getComplianceProfileOrThrow(playerId);

        ComplianceFlag flag = complianceFlagRepository
                .findByFlagIdAndComplianceId(flagId, profile.getComplianceId())
                .orElseThrow(() -> new ComplianceFlagMissingException(flagId));

        ComplianceFlagType newType = request.type() != null
                ? request.type()
                : flag.getType();

        ComplianceFlagSeverity newSeverity = request.severity() != null
                ? request.severity()
                : flag.getSeverity();

        OffsetDateTime newResolvedDate = request.resolvedDate() != null
                ? request.resolvedDate()
                : flag.getResolvedDate();

        validateResolvedDate(flag.getCreatedDate(), newResolvedDate);

        ensureNoDuplicateActiveFlag(
                playerId,
                profile.getComplianceId(),
                newType,
                newSeverity,
                flagId
        );

        if (request.type() != null) {
            flag.setType(request.type());
        }

        if (request.severity() != null) {
            flag.setSeverity(request.severity());
        }

        if (request.resolvedDate() != null) {
            flag.setResolvedDate(request.resolvedDate());
        }

        ComplianceFlag savedFlag = complianceFlagRepository.save(flag);

        return toDto(savedFlag);
    }

    private ComplianceProfile getComplianceProfileOrThrow(Long playerId) {
        return complianceProfileRepository
                .findByPlayerId(playerId)
                .orElseThrow(() -> new ComplianceProfileMissingException(playerId));
    }

    private void ensureNoDuplicateActiveFlag(
            Long playerId,
            Long complianceId,
            ComplianceFlagType type,
            ComplianceFlagSeverity severity,
            Long excludedFlagId
    ) {
        List<ComplianceFlag> existingFlags = complianceFlagRepository
                .findByComplianceIdAndTypeAndSeverity(complianceId, type, severity);

        boolean duplicateExists = existingFlags.stream()
                .anyMatch(flag ->
                        !Objects.equals(flag.getFlagId(), excludedFlagId)
                                && flag.getResolvedDate() == null
                );

        if (duplicateExists) {
            throw new ComplianceFlagExistsException(playerId, type, severity);
        }
    }

    private void validateResolvedDate(
            OffsetDateTime createdDate,
            OffsetDateTime resolvedDate
    ) {
        if (resolvedDate != null && resolvedDate.isBefore(createdDate)) {
            throw new InvalidComplianceFlagException(
                    "Resolved date cannot be before created date"
            );
        }
    }

    private ComplianceFlagDto toDto(ComplianceFlag flag) {
        return new ComplianceFlagDto(
                flag.getFlagId(),
                flag.getComplianceId(),
                flag.getType(),
                flag.getSeverity(),
                flag.getCreatedDate(),
                flag.getResolvedDate()
        );
    }
}