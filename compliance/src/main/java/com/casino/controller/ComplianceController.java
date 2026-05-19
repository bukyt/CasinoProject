package com.casino.controller;

import com.casino.api.ComplianceApi;
import com.casino.dto.profile.ComplianceProfileDto;
import com.casino.dto.profile.CreateComplianceProfileDTO;
import com.casino.dto.profile.EligibilityResponseDTO;
import com.casino.dto.profile.ModifyComplianceProfileDTO;
import com.casino.service.ComplianceService;
import com.casino.service.EligibilityService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ComplianceController implements ComplianceApi {

    private final ComplianceService complianceService;
    private final EligibilityService eligibilityService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceProfileDto> createComplianceProfile(
        @Valid @RequestBody CreateComplianceProfileDTO createComplianceProfileDTO) {

        val profile = complianceService.createComplianceProfile(createComplianceProfileDTO.playerProfileId());
        return new ResponseEntity<>(profile, HttpStatus.CREATED);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceProfileDto> getComplianceProfile(@PathVariable("playerId") Long playerId) {
        val profile = complianceService.getComplianceProfile(playerId);
        return ResponseEntity.ok(profile);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ComplianceProfileDto> modifyComplianceProfile(
        @PathVariable Long playerId,
        @RequestBody ModifyComplianceProfileDTO modifyComplianceProfileDTO) {
        val profile = complianceService.modifyComplianceProfile(playerId, modifyComplianceProfileDTO);
        return ResponseEntity.ok(profile);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EligibilityResponseDTO> checkEligibility(Long playerId) {
        return ResponseEntity.ok(eligibilityService.checkEligibility(playerId));
    }
}
