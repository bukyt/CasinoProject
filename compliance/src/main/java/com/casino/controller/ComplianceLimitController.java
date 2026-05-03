package com.casino.controller;

import com.casino.api.ComplianceLimitApi;
import com.casino.dto.ComplianceLimitDto;
import com.casino.dto.CreateComplianceLimitDTO;
import com.casino.dto.ModifyComplianceLimitDTO;
import com.casino.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComplianceLimitController implements ComplianceLimitApi {

    private final LimitService complianceLimitService;

    @Override
    public ResponseEntity<ComplianceLimitDto> createComplianceLimit(
            Long playerId,
            CreateComplianceLimitDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(complianceLimitService.createComplianceLimit(playerId, request));
    }

    @Override
    public ResponseEntity<List<ComplianceLimitDto>> getComplianceLimits(Long playerId) {
        return ResponseEntity.ok(complianceLimitService.getComplianceLimits(playerId));
    }

    @Override
    public ResponseEntity<ComplianceLimitDto> modifyComplianceLimit(
            Long playerId,
            Long limitId,
            ModifyComplianceLimitDTO request
    ) {
        return ResponseEntity.ok(
                complianceLimitService.modifyComplianceLimit(playerId, limitId, request)
        );
    }
}