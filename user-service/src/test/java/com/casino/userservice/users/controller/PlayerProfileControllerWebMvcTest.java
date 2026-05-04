package com.casino.userservice.users.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import com.casino.userservice.users.dto.PlayerProfileResponse;
import com.casino.userservice.users.model.ProfileStatus;
import com.casino.userservice.users.service.PlayerProfileService;

@WebMvcTest(PlayerProfileController.class)
class PlayerProfileControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerProfileService playerProfileService;

    @Test
    void getProfileReturnsOkWhenProfileExists() throws Exception {
        PlayerProfileResponse response = PlayerProfileResponse.builder()
                .playerProfileId("pp-01")
                .accountId("acc-01")
                .fullName("Demo Player One")
                .dateOfBirth(LocalDate.of(1995, 5, 10))
                .status(ProfileStatus.ACTIVE)
                .email("player1@example.com")
                .phone("+3725000001")
                .address("Tallinn")
                .language("en")
                .currency("EUR")
                .build();

        when(playerProfileService.getProfile("pp-01")).thenReturn(response);

        mockMvc.perform(get("/profiles/pp-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerProfileId").value("pp-01"))
                .andExpect(jsonPath("$.accountId").value("acc-01"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getProfileReturnsNotFoundWhenServiceThrowsNotFound() throws Exception {
        when(playerProfileService.getProfile("missing-id"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        mockMvc.perform(get("/profiles/missing-id"))
                .andExpect(status().isNotFound());
    }
}
