package com.casino.service;

import com.casino.dto.EligibilityBlockReason;
import com.casino.dto.EligibilityResponseDTO;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.ComplianceProfile;
import com.casino.model.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {

    private final ComplianceProfileRepository complianceProfileRepository;

    public EligibilityService(ComplianceProfileRepository complianceProfileRepository) {
        this.complianceProfileRepository = complianceProfileRepository;
    }

    @Transactional(readOnly = true)
    public EligibilityResponseDTO checkEligibility(Long playerProfileId) {
        return complianceProfileRepository.findFirstByPlayerProfileId(playerProfileId)
                .map(this::evaluateEligibility)
                .orElseThrow(() -> new ComplianceProfileMissingException(playerProfileId));
    }

    private EligibilityResponseDTO evaluateEligibility(ComplianceProfile profile) {
        List<EligibilityBlockReason> blockReasons = new ArrayList<>();

        if (!profile.isAgeVerified()) {
            blockReasons.add(EligibilityBlockReason.AGE_NOT_VERIFIED);
        }

        if (profile.isSelfExcluded()) {
            blockReasons.add(EligibilityBlockReason.SELF_EXCLUDED);
        }

        if (profile.getRiskLevel() == null
                || profile.getRiskLevel() == ComplianceProfileRiskLevel.UNASSESSED) {
            blockReasons.add(EligibilityBlockReason.AML_REVIEW_REQUIRED);
        }

        if (profile.getRiskLevel() == ComplianceProfileRiskLevel.HIGH) {
            blockReasons.add(EligibilityBlockReason.HIGH_RISK_PROFILE);
            blockReasons.add(EligibilityBlockReason.AML_REVIEW_REQUIRED);
        }

        if (profile.getRiskLevel() == ComplianceProfileRiskLevel.CRITICAL) {
            blockReasons.add(EligibilityBlockReason.CRITICAL_RISK_PROFILE);
            blockReasons.add(EligibilityBlockReason.AML_REVIEW_REQUIRED);
        }

        boolean mayBet = canBet(profile, blockReasons);
        boolean mayWithdraw = canWithdraw(profile);

        return new EligibilityResponseDTO(
                profile.getPlayerProfileId(),
                mayBet,
                mayWithdraw,
                profile.getRiskLevel(),
                profile.isAgeVerified(),
                profile.isSelfExcluded(),
                blockReasons,
                OffsetDateTime.now()
        );
    }

    private boolean canBet(
            ComplianceProfile profile,
            List<EligibilityBlockReason> blockReasons
    ) {
        return blockReasons.isEmpty()
                && profile.isAgeVerified()
                && !profile.isSelfExcluded()
                && profile.getRiskLevel() != ComplianceProfileRiskLevel.HIGH
                && profile.getRiskLevel() != ComplianceProfileRiskLevel.CRITICAL;
    }

    private boolean canWithdraw(ComplianceProfile profile) {
        return profile.isAgeVerified()
                && profile.getRiskLevel() != ComplianceProfileRiskLevel.CRITICAL;
    }


}