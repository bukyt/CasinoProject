package com.casino.service;


import com.casino.dto.ModifyComplianceProfileDTO;
import com.casino.exceptions.profile.ComplianceProfileExistsException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.ComplianceProfile;
import com.casino.model.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceProfileRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ComplianceService {

    private final ComplianceProfileRepository complianceProfileRepository;

    @Transactional
    public ComplianceProfile createComplianceProfile(Long playerProfileId) {
        List<ComplianceProfile> complianceProfiles = complianceProfileRepository.findByPlayerProfileId(playerProfileId);

        if (!complianceProfiles.isEmpty()) {
            throw new ComplianceProfileExistsException(playerProfileId);
        }

        ComplianceProfile newProfile = new ComplianceProfile(null, playerProfileId, false, false,
                ComplianceProfileRiskLevel.UNASSESSED, OffsetDateTime.now());
        complianceProfileRepository.saveAndFlush(newProfile);

        return newProfile;
    }

    @Transactional()
    public ComplianceProfile getComplianceProfile(Long playerProfileId) {
        val profile = complianceProfileRepository.findFirstByPlayerProfileId(playerProfileId);
        if (profile.isEmpty()) {
            throw new ComplianceProfileMissingException(playerProfileId);
        }
        return profile.get();
    }

    @Transactional
    public ComplianceProfile modifyComplianceProfile(
            Long playerId,
            ModifyComplianceProfileDTO request
    ) {
        ComplianceProfile profile = complianceProfileRepository
                .findFirstByPlayerProfileId(playerId)
                .orElseThrow(() -> new ComplianceProfileMissingException(playerId));

        if (request.getAgeVerified() != null) {
            profile.setAgeVerified(request.getAgeVerified());
        }

        if (request.getSelfExcluded() != null) {
            profile.setSelfExcluded(request.getSelfExcluded());
        }

        if (request.getRiskLevel() != null) {
            profile.setRiskLevel(request.getRiskLevel());
        }

        profile.setLastReviewDate(OffsetDateTime.now());

        return complianceProfileRepository.save(profile);
    }


}
