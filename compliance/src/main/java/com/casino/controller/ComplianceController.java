package com.casino.controller;

import com.casino.api.ComplianceApi;
import com.casino.dto.ComplianceProfileDto;
import com.casino.dto.CreateComplianceProfileDTO;
import com.casino.dto.EligibilityResponseDTO;
import com.casino.dto.ModifyComplianceProfileDTO;
import com.casino.model.ComplianceProfile;
import com.casino.service.ComplianceService;
import com.casino.service.EligibilityService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ComplianceController implements ComplianceApi {

    private final ComplianceService complianceService;
    private final EligibilityService eligibilityService;

    @Override
    public ResponseEntity<ComplianceProfileDto> createComplianceProfile(
            @Valid @RequestBody CreateComplianceProfileDTO createComplianceProfileDTO) {

        val profile = complianceService.createComplianceProfile(createComplianceProfileDTO.playerProfileId());
        return new ResponseEntity<>(toDto(profile), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ComplianceProfileDto> getComplianceProfile(@PathVariable("playerId") Long playerId) {
        val profile = complianceService.getComplianceProfile(playerId);
        return ResponseEntity.ok(toDto(profile));
    }

    @Override
    public ResponseEntity<ComplianceProfileDto> modifyComplianceProfile(
            @PathVariable Long playerId,
            @RequestBody ModifyComplianceProfileDTO modifyComplianceProfileDTO) {
        val profile = complianceService.modifyComplianceProfile(playerId, modifyComplianceProfileDTO);
        return ResponseEntity.ok(toDto(profile));
    }

    @Override
    public ResponseEntity<EligibilityResponseDTO> checkEligibility(Long playerId) {
        return ResponseEntity.ok(eligibilityService.checkEligibility(playerId));
    }


    private ComplianceProfileDto toDto(ComplianceProfile profile) {
        return new ComplianceProfileDto(
                profile.getComplianceId(),
                profile.getPlayerProfileId(),
                profile.isAgeVerified(),
                profile.isSelfExcluded(),
                profile.getRiskLevel(),
                profile.getLastReviewDate()
        );
    }


}
