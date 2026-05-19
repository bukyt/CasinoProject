package com.casino.controller;

import com.casino.api.ComplianceLimitApi;
import com.casino.dto.limit.CreateGamblingLimitDTO;
import com.casino.dto.limit.GamblingLimitDto;
import com.casino.dto.limit.ModifyGamblingLimitDTO;
import com.casino.service.LimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ComplianceLimitController implements ComplianceLimitApi {

    private final LimitService complianceLimitService;

    @Override
    @PreAuthorize("@profileSecurity.isOwnerOrAdmin(authentication, #playerId)")
    public ResponseEntity<GamblingLimitDto> createComplianceLimit(
        Long playerId,
        CreateGamblingLimitDTO request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(complianceLimitService.createComplianceLimit(playerId, request));
    }

    @Override
    @PreAuthorize("@profileSecurity.isOwnerOrAdmin(authentication, #playerId)")
    public ResponseEntity<List<GamblingLimitDto>> getComplianceLimits(Long playerId) {
        return ResponseEntity.ok(complianceLimitService.getComplianceLimits(playerId));
    }

    @Override
    @PreAuthorize("@profileSecurity.isOwnerOrAdmin(authentication, #playerId)")
    public ResponseEntity<GamblingLimitDto> modifyComplianceLimit(
        Long playerId,
        Long limitId,
        ModifyGamblingLimitDTO request
    ) {
        return ResponseEntity.ok(
            complianceLimitService.modifyComplianceLimit(playerId, limitId, request)
        );
    }
}
