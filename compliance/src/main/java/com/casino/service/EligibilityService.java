package com.casino.service;

import com.casino.dto.EligibilityBlockReason;
import com.casino.dto.EligibilityResponseDTO;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.ComplianceProfile;
import com.casino.model.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

import static com.casino.dto.EligibilityBlockReason.*;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final ComplianceProfileRepository complianceProfileRepository;

    @Transactional(readOnly = true)
    public EligibilityResponseDTO checkEligibility(Long playerProfileId) {
        ComplianceProfile profile = complianceProfileRepository
            .findFirstByPlayerProfileId(playerProfileId)
            .orElseThrow(() -> new ComplianceProfileMissingException(playerProfileId));

        return evaluateEligibility(profile);
    }

    private EligibilityResponseDTO evaluateEligibility(ComplianceProfile profile) {
        Set<EligibilityBlockReason> blockReasons = EnumSet.noneOf(EligibilityBlockReason.class);

        addProfileBlockReasons(profile, blockReasons);

        boolean mayBet = mayBet(blockReasons);
        boolean mayWithdraw = mayWithdraw(blockReasons);

        return new EligibilityResponseDTO(
            profile.getPlayerProfileId(),
            mayBet,
            mayWithdraw,
            profile.getRiskLevel(),
            profile.isAgeVerified(),
            profile.isSelfExcluded(),
            new ArrayList<>(blockReasons),
            OffsetDateTime.now()
        );
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

    private boolean mayBet(Set<EligibilityBlockReason> blockReasons) {
        return !blockReasons.contains(AGE_NOT_VERIFIED)
            && !blockReasons.contains(SELF_EXCLUDED)
            && !blockReasons.contains(HIGH_RISK_PROFILE)
            && !blockReasons.contains(CRITICAL_RISK_PROFILE)
            && !blockReasons.contains(AML_REVIEW_REQUIRED);
    }

    private boolean mayWithdraw(Set<EligibilityBlockReason> blockReasons) {
        return !blockReasons.contains(AGE_NOT_VERIFIED)
            && !blockReasons.contains(CRITICAL_RISK_PROFILE)
            && !blockReasons.contains(AML_REVIEW_REQUIRED);
    }
}