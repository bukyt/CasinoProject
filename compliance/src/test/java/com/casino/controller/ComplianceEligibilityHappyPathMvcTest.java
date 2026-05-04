package com.casino.controller;

import com.casino.dto.profile.EligibilityLimitDTO;
import com.casino.dto.profile.EligibilityResponseDTO;
import com.casino.model.limit.GamblingLimitPeriod;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.service.ComplianceService;
import com.casino.service.EligibilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComplianceController.class)
class ComplianceEligibilityHappyPathMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EligibilityService eligibilityService;

    @MockitoBean
    private ComplianceService complianceService;

    @Test
    void checkEligibility_shouldReturnEligiblePlayerWithActiveLimits() throws Exception {
        Long playerId = 123L;

        EligibilityLimitDTO activeBetLimit = new EligibilityLimitDTO(
            100,
            GamblingLimitPeriod.DAILY
        );

        EligibilityLimitDTO activeWithdrawalLimit = new EligibilityLimitDTO(
            500,
            GamblingLimitPeriod.WEEKLY
        );

        EligibilityResponseDTO response = new EligibilityResponseDTO(
            playerId,
            true,
            true,
            ComplianceProfileRiskLevel.LOW,
            true,
            false,
            List.of(),
            activeBetLimit,
            activeWithdrawalLimit,
            OffsetDateTime.parse("2026-05-01T12:30:00+03:00")
        );

        when(eligibilityService.checkEligibility(playerId)).thenReturn(response);

        mockMvc.perform(get("/compliance/{playerId}/eligibility", playerId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.playerProfileId").value(123))
            .andExpect(jsonPath("$.mayBet").value(true))
            .andExpect(jsonPath("$.mayWithdraw").value(true))
            .andExpect(jsonPath("$.riskLevel").value("LOW"))
            .andExpect(jsonPath("$.ageVerified").value(true))
            .andExpect(jsonPath("$.selfExcluded").value(false))
            .andExpect(jsonPath("$.blockReasons").isArray())
            .andExpect(jsonPath("$.blockReasons").isEmpty())

            .andExpect(jsonPath("$.activeBetLimit.amount").value(100))
            .andExpect(jsonPath("$.activeBetLimit.period").value("DAILY"))

            .andExpect(jsonPath("$.activeWithdrawalLimit.amount").value(500))
            .andExpect(jsonPath("$.activeWithdrawalLimit.period").value("WEEKLY"))

            .andExpect(jsonPath("$.checkedAt").exists());
    }
}