package com.casino.service;

import com.casino.dto.limit.GamblingLimitDto;
import com.casino.dto.profile.EligibilityBlockReason;
import com.casino.dto.profile.EligibilityLimitDTO;
import com.casino.dto.profile.EligibilityResponseDTO;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.limit.GamblingLimit;
import com.casino.model.limit.GamblingLimitType;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import com.casino.repository.ComplianceProfileRepository;
import com.casino.repository.GamblingLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.casino.dto.profile.EligibilityBlockReason.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final ComplianceProfileRepository complianceProfileRepository;
    private final GamblingLimitRepository gamblingLimitRepository;
    private final ComplianceFlagRepository complianceFlagRepository;

    public static GamblingLimitDto toDto(GamblingLimit limit) {
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

    @Transactional(readOnly = true)
    public EligibilityResponseDTO checkEligibility(Long playerProfileId) {
        log.info("Checking eligibility for player profile {}", playerProfileId);
        ComplianceProfile profile = complianceProfileRepository
            .findFirstByPlayerProfileId(playerProfileId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerProfileId));

        OffsetDateTime now = OffsetDateTime.now();

        List<GamblingLimit> activeLimits = gamblingLimitRepository.findActiveByComplianceId(
            profile.getComplianceId(),
            now
        );

        List<ComplianceFlag> flags = complianceFlagRepository.findByComplianceProfile_ComplianceId(
            profile.getComplianceId()
        );

        log.info("Returning eligibility for player profile {}", playerProfileId);
        return evaluateEligibility(profile, activeLimits, flags, now);
    }

    private EligibilityResponseDTO evaluateEligibility(
        ComplianceProfile profile,
        List<GamblingLimit> activeLimits,
        List<ComplianceFlag> flags,
        OffsetDateTime checkedAt
    ) {
        Set<EligibilityBlockReason> blockReasons =
            EnumSet.noneOf(EligibilityBlockReason.class);

        addProfileBlockReasons(profile, blockReasons);
        addFlagBlockReasons(flags, blockReasons);
        addLimitBlockReasons(activeLimits, blockReasons);

        boolean mayBet = mayBet(blockReasons);
        boolean mayWithdraw = mayWithdraw(blockReasons);

        EligibilityLimitDTO activeBetLimit = findActiveLimitByType(
            activeLimits,
            GamblingLimitType.BET
        );

        EligibilityLimitDTO activeWithdrawalLimit = findActiveLimitByType(
            activeLimits,
            GamblingLimitType.WITHDRAWAL
        );

        return new EligibilityResponseDTO(
            profile.getPlayerProfileId(),
            mayBet,
            mayWithdraw,
            profile.getRiskLevel(),
            profile.isAgeVerified(),
            profile.isSelfExcluded(),
            new ArrayList<>(blockReasons),
            activeBetLimit,
            activeWithdrawalLimit,
            checkedAt
        );
    }

    private EligibilityLimitDTO findActiveLimitByType(
        List<GamblingLimit> activeLimits,
        GamblingLimitType type
    ) {
        return activeLimits.stream()
            .filter(limit -> limit.getType() == type)
            .findFirst()
            .map(limit -> new EligibilityLimitDTO(
                limit.getAmount(),
                limit.getPeriod()
            ))
            .orElse(null);
    }

    private void addProfileBlockReasons(
        ComplianceProfile profile,
        Set<EligibilityBlockReason> blockReasons
    ) {
        if (!profile.isAgeVerified()) {
            blockReasons.add(AGE_NOT_VERIFIED);
        }

        if (profile.isSelfExcluded()) {
            blockReasons.add(SELF_EXCLUDED);
        }

        ComplianceProfileRiskLevel riskLevel = profile.getRiskLevel();

        if (riskLevel == null || riskLevel == ComplianceProfileRiskLevel.UNASSESSED) {
            blockReasons.add(AML_REVIEW_REQUIRED);
            return;
        }

        if (riskLevel == ComplianceProfileRiskLevel.HIGH) {
            blockReasons.add(HIGH_RISK_PROFILE);
            blockReasons.add(AML_REVIEW_REQUIRED);
            return;
        }

        if (riskLevel == ComplianceProfileRiskLevel.CRITICAL) {
            blockReasons.add(CRITICAL_RISK_PROFILE);
            blockReasons.add(AML_REVIEW_REQUIRED);
        }
    }

    private void addFlagBlockReasons(
        List<ComplianceFlag> flags,
        Set<EligibilityBlockReason> blockReasons
    ) {
        boolean hasUnresolvedHighFlag = flags.stream()
            .anyMatch(flag ->
                isUnresolved(flag)
                    && flag.getSeverity() == ComplianceFlagSeverity.HIGH
            );

        if (hasUnresolvedHighFlag) {
            blockReasons.add(HIGH_RISK_PROFILE);
            blockReasons.add(AML_REVIEW_REQUIRED);
        }

        boolean hasUnresolvedMediumFlag = flags.stream()
            .anyMatch(flag ->
                isUnresolved(flag)
                    && flag.getSeverity() == ComplianceFlagSeverity.MEDIUM
            );

        if (hasUnresolvedMediumFlag) {
            blockReasons.add(AML_REVIEW_REQUIRED);
        }
    }

    private void addLimitBlockReasons(
        List<GamblingLimit> activeLimits,
        Set<EligibilityBlockReason> blockReasons
    ) {
        boolean hasZeroBetLimit = activeLimits.stream()
            .anyMatch(limit ->
                limit.getType() == GamblingLimitType.BET
                    && limit.getAmount() <= 0
            );

        if (hasZeroBetLimit) {
            blockReasons.add(BET_LIMIT_ZERO);
        }

        boolean hasZeroWithdrawal = activeLimits.stream()
            .anyMatch(limit ->
                limit.getType() == GamblingLimitType.WITHDRAWAL
                    && limit.getAmount() <= 0
            );

        if (hasZeroWithdrawal) {
            blockReasons.add(WITHDRAWAL_LIMIT_ZERO);
        }
    }

    private boolean mayBet(Set<EligibilityBlockReason> blockReasons) {
        return !blockReasons.contains(AGE_NOT_VERIFIED)
            && !blockReasons.contains(SELF_EXCLUDED)
            && !blockReasons.contains(HIGH_RISK_PROFILE)
            && !blockReasons.contains(CRITICAL_RISK_PROFILE)
            && !blockReasons.contains(AML_REVIEW_REQUIRED)
            && !blockReasons.contains(BET_LIMIT_ZERO);
    }

    private boolean mayWithdraw(Set<EligibilityBlockReason> blockReasons) {
        return !blockReasons.contains(AGE_NOT_VERIFIED)
            && !blockReasons.contains(CRITICAL_RISK_PROFILE)
            && !blockReasons.contains(AML_REVIEW_REQUIRED)
            && !blockReasons.contains(WITHDRAWAL_LIMIT_ZERO);
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
}