package com.casino.profileservice.users.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.casino.profileservice.integration.AuthAccountClient;
import com.casino.profileservice.integration.WalletClient;
import com.casino.profileservice.security.ProfileAuth;
import com.casino.profileservice.users.dto.PlayerProfileRequest;
import com.casino.profileservice.users.dto.PlayerProfileResponse;
import com.casino.profileservice.users.model.PlayerProfile;
import com.casino.profileservice.users.model.ProfileStatus;
import com.casino.profileservice.users.repository.PlayerProfileRepository;

@ExtendWith(MockitoExtension.class)
class PlayerProfileServiceTest {

    @Mock
    private PlayerProfileRepository playerProfileRepository;

    @Mock
    private AuthAccountClient authAccountClient;

    @Mock
    private WalletClient walletClient;

    @Mock
    private ProfileAuth profileAuth;

    @InjectMocks
    private PlayerProfileService playerProfileService;

    @Test
    void createProfileCreatesWalletForNewPlayerProfile() {
        PlayerProfileRequest request = new PlayerProfileRequest();
        request.setAccountId("acc-01");
        request.setFullName("Demo Player");
        request.setDateOfBirth(LocalDate.of(1995, 5, 10));
        request.setStatus(ProfileStatus.ACTIVE);
        request.setEmail("player@example.com");
        request.setLanguage("en");
        request.setCurrency("EUR");

        when(playerProfileRepository.existsByAccountId("acc-01")).thenReturn(false);
        when(playerProfileRepository.saveAndFlush(any(PlayerProfile.class))).thenAnswer(invocation -> {
            PlayerProfile profile = invocation.getArgument(0);
            profile.setPlayerProfileId(42);
            return profile;
        });

        PlayerProfileResponse response = playerProfileService.createProfile(request, "Bearer token");

        assertEquals(42, response.getPlayerProfileId());
        verify(walletClient).createWallet(42);
    }
}
