package com.casino.controller;

import com.casino.api.ComplianceFlagApi;
import com.casino.dto.ComplianceFlagDto;
import com.casino.dto.CreateComplianceFlagDTO;
import com.casino.dto.ModifyComplianceFlagDTO;
import com.casino.service.FlagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ComplianceFlagController implements ComplianceFlagApi {

    private final FlagService complianceFlagService;
 
    @Override
    public ResponseEntity<ComplianceFlagDto> createComplianceFlag(
        Long playerId,
        CreateComplianceFlagDTO request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(complianceFlagService.createComplianceFlag(playerId, request));
    }

    @Override
    public ResponseEntity<ComplianceFlagDto> modifyComplianceFlag(
        Long playerId,
        Long flagId,
        ModifyComplianceFlagDTO request
    ) {
        return ResponseEntity.ok(
            complianceFlagService.modifyComplianceFlag(playerId, flagId, request)
        );
    }
}