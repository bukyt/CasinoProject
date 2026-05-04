package com.casino.service;

import com.casino.dto.flag.ComplianceFlagDto;
import com.casino.dto.limit.GamblingLimitDto;
import com.casino.dto.profile.ComplianceProfileDto;
import com.casino.dto.profile.ModifyComplianceProfileDTO;
import com.casino.exceptions.profile.ComplianceProfileExistsException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import com.casino.model.limit.GamblingLimit;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import com.casino.repository.ComplianceProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ComplianceService {

    private final ComplianceProfileRepository complianceProfileRepository;
    private final ComplianceFlagRepository complianceFlagRepository;

    @Transactional
    public ComplianceProfileDto createComplianceProfile(Long playerProfileId) {
        List<ComplianceProfile> complianceProfiles =
            complianceProfileRepository.findByPlayerProfileId(playerProfileId);

        if (!complianceProfiles.isEmpty()) {
            throw new ComplianceProfileExistsException(playerProfileId);
        }

        OffsetDateTime now = OffsetDateTime.now();

        ComplianceProfile newProfile = new ComplianceProfile();
        newProfile.setPlayerProfileId(playerProfileId);
        newProfile.setAgeVerified(false);
        newProfile.setSelfExcluded(false);
        newProfile.setRiskLevel(ComplianceProfileRiskLevel.UNASSESSED);
        newProfile.setLastReviewDate(now);

        ComplianceProfile savedProfile = complianceProfileRepository.save(newProfile);

        return toDto(savedProfile);
    }

    @Transactional(readOnly = true)
    public ComplianceProfileDto getComplianceProfile(Long playerProfileId) {
        ComplianceProfile profile = complianceProfileRepository
            .findFirstByPlayerProfileId(playerProfileId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerProfileId));

        return toDto(profile);
    }

    @Transactional
    public ComplianceProfileDto modifyComplianceProfile(
        Long playerId,
        ModifyComplianceProfileDTO request
    ) {
        ComplianceProfile profile = complianceProfileRepository
            .findFirstByPlayerProfileId(playerId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerId));

        OffsetDateTime now = OffsetDateTime.now();

        if (request.ageVerified() != null) {
            profile.setAgeVerified(request.ageVerified());

            // Optional later:
            // if ageVerified == false, create NO_AGE_VERIFICATION flag
            // if ageVerified == true, resolve NO_AGE_VERIFICATION flag
        }

        if (request.selfExcluded() != null) {
            boolean previousSelfExcluded = profile.isSelfExcluded();
            boolean newSelfExcluded = request.selfExcluded();

            profile.setSelfExcluded(newSelfExcluded);

            if (!previousSelfExcluded && newSelfExcluded) {
                createFlagIfNoUnresolvedFlagOfTypeExists(
                    profile,
                    ComplianceFlagType.SELF_EXCLUSION,
                    ComplianceFlagSeverity.HIGH,
                    now
                );
            }

            if (previousSelfExcluded && !newSelfExcluded) {
                resolveUnresolvedFlagsOfType(
                    profile.getComplianceId(),
                    ComplianceFlagType.SELF_EXCLUSION,
                    ComplianceFlagSeverity.RESOLVED_ADMIN,
                    now
                );
            }
        }

        if (request.riskLevel() != null) {
            profile.setRiskLevel(request.riskLevel());

            resolveUnresolvedFlagsOfType(
                profile.getComplianceId(),
                ComplianceFlagType.MANUAL_RISK_LEVEL,
                ComplianceFlagSeverity.RESOLVED_ADMIN,
                now
            );

            createFlag(
                profile,
                ComplianceFlagType.MANUAL_RISK_LEVEL,
                toFlagSeverity(request.riskLevel()),
                now
            );
        }

        profile.setLastReviewDate(now);

        ComplianceProfile savedProfile = complianceProfileRepository.save(profile);

        return toDto(savedProfile);
    }


    private void createFlagIfNoUnresolvedFlagOfTypeExists(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity,
        OffsetDateTime now
    ) {
        boolean unresolvedFlagExists = complianceFlagRepository
            .findByComplianceProfile_ComplianceIdAndType(
                profile.getComplianceId(),
                type
            )
            .stream()
            .anyMatch(this::isUnresolved);

        if (!unresolvedFlagExists) {
            createFlag(profile, type, severity, now);
        }
    }

    private void createFlag(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity,
        OffsetDateTime now
    ) {
        ComplianceFlag flag = new ComplianceFlag();
        flag.setComplianceProfile(profile);
        flag.setType(type);
        flag.setSeverity(severity);
        flag.setCreatedDate(now);
        flag.setResolvedDate(null);

        complianceFlagRepository.save(flag);
    }

    private void resolveUnresolvedFlagsOfType(
        Long complianceId,
        ComplianceFlagType type,
        ComplianceFlagSeverity resolvedSeverity,
        OffsetDateTime now
    ) {
        List<ComplianceFlag> flagsToResolve = complianceFlagRepository
            .findByComplianceProfile_ComplianceIdAndType(complianceId, type)
            .stream()
            .filter(this::isUnresolved)
            .toList();

        flagsToResolve.forEach(flag -> {
            flag.setSeverity(resolvedSeverity);
            flag.setResolvedDate(now);
        });

        complianceFlagRepository.saveAll(flagsToResolve);
    }

    private boolean isUnresolved(ComplianceFlag flag) {
        return flag.getResolvedDate() == null
            && flag.getSeverity() != ComplianceFlagSeverity.RESOLVED
            && flag.getSeverity() != ComplianceFlagSeverity.RESOLVED_ADMIN;
    }

    private ComplianceFlagSeverity toFlagSeverity(ComplianceProfileRiskLevel riskLevel) {
        if (riskLevel == null) {
            return ComplianceFlagSeverity.MEDIUM;
        }

        return switch (riskLevel) {
            case LOW -> ComplianceFlagSeverity.LOW;
            case HIGH, CRITICAL -> ComplianceFlagSeverity.HIGH;
            case UNASSESSED, MEDIUM -> ComplianceFlagSeverity.MEDIUM;
        };
    }

    private ComplianceProfileDto toDto(ComplianceProfile profile) {
        return new ComplianceProfileDto(
            profile.getComplianceId(),
            profile.getPlayerProfileId(),
            profile.isAgeVerified(),
            profile.isSelfExcluded(),
            profile.getRiskLevel(),
            profile.getLastReviewDate(),
            toLimitDtos(profile.getLimits()),
            toFlagDtos(profile.getFlags())
        );
    }

    private List<GamblingLimitDto> toLimitDtos(List<GamblingLimit> limits) {
        if (limits == null) {
            return List.of();
        }

        return limits.stream()
            .map(this::toDto)
            .toList();
    }

    private GamblingLimitDto toDto(GamblingLimit limit) {
        return new GamblingLimitDto(
            limit.getComplianceProfile().getComplianceId(),
            limit.getLimitId(),
            limit.getType(),
            limit.getAmount(),
            limit.getPeriod(),
            limit.getCreatedDate(),
            limit.getStartDate(),
            limit.getEndDate(),
            limit.getRevokedDate()
        );
    }

    private List<ComplianceFlagDto> toFlagDtos(List<ComplianceFlag> flags) {
        if (flags == null) {
            return List.of();
        }

        return flags.stream()
            .map(this::toDto)
            .toList();
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