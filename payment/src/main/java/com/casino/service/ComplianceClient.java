package com.casino.service;

import com.casino.dto.eligibility.EligibilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ComplianceClient {

    private final RestClient restClient;

    public EligibilityResponseDTO getEligibility(Long playerProfileId) {
        return restClient.get()
            .uri("/compliance/{playerId}/eligibility", playerProfileId)
            .retrieve()
            .body(EligibilityResponseDTO.class);
    }
}