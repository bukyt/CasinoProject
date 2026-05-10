package com.casino.controller;

import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.service.ComplianceService;
import com.casino.service.EligibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComplianceController.class)
class ComplianceEligibilityErrorMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EligibilityService eligibilityService;

    @MockitoBean
    private ComplianceService complianceService;

    @Test
    void checkEligibility_shouldReturnNotFoundWhenComplianceProfileDoesNotExist() throws Exception {
        Long playerId = 999L;

        when(eligibilityService.checkEligibility(playerId))
            .thenThrow(new ComplianceProfileMissingException(playerId));

        mockMvc.perform(get("/compliance/{playerId}/eligibility", playerId))
            .andExpect(status().isNotFound());
    }
}