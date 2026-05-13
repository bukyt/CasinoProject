package com.casino.profileservice.users.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.casino.profileservice.integration.AuthAccountClient;
import com.casino.profileservice.users.dto.ContactDetailsRequest;
import com.casino.profileservice.users.dto.PlayerProfileRequest;
import com.casino.profileservice.users.dto.PlayerProfileResponse;
import com.casino.profileservice.users.dto.PreferencesRequest;
import com.casino.profileservice.users.model.ContactDetails;
import com.casino.profileservice.users.model.PlayerProfile;
import com.casino.profileservice.users.model.Preferences;
import com.casino.profileservice.users.model.ProfileStatus;
import com.casino.profileservice.users.repository.PlayerProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

@Service
public class PlayerProfileService {

    private static final Logger log = LoggerFactory.getLogger(PlayerProfileService.class);

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private AuthAccountClient authAccountClient;

    public PlayerProfileResponse createProfile(PlayerProfileRequest request, String authorizationHeader) {
        authAccountClient.verifyAccountExists(request.getAccountId(), authorizationHeader);
        if (playerProfileRepository.existsByAccountId(request.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Profile already exists for accountId: " + request.getAccountId());
        }

        PlayerProfile profile = PlayerProfile.builder()
                .accountId(request.getAccountId())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .status(request.getStatus())
                .contactDetails(ContactDetails.builder()
                        .email(request.getEmail())
                        .phone(request.getPhone())
                        .address(request.getAddress())
                        .build())
                .preferences(Preferences.builder()
                        .language(request.getLanguage())
                        .currency(request.getCurrency())
                        .build())
                .build();

        return mapToDto(playerProfileRepository.save(profile));
    }

    public PlayerProfileResponse getProfile(Integer playerProfileId) {
        return mapToDto(requireProfile(playerProfileId));
    }

    public PlayerProfileResponse updateProfile(Integer playerProfileId, PlayerProfileRequest request) {
        PlayerProfile profile = requireProfile(playerProfileId);

        if (playerProfileRepository.existsByAccountIdAndPlayerProfileIdNot(request.getAccountId(), playerProfileId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another profile already uses accountId: " + request.getAccountId());
        }

        profile.setAccountId(request.getAccountId());
        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setStatus(request.getStatus());
        profile.setContactDetails(ContactDetails.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
        profile.setPreferences(Preferences.builder()
                .language(request.getLanguage())
                .currency(request.getCurrency())
                .build());

        return mapToDto(playerProfileRepository.save(profile));
    }

    public PlayerProfileResponse updateContact(Integer playerProfileId, ContactDetailsRequest request) {
        PlayerProfile profile = requireProfile(playerProfileId);

        ContactDetails contactDetails = profile.getContactDetails() == null ? new ContactDetails()
                : profile.getContactDetails();
        if (request.getEmail() != null) {
            contactDetails.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            contactDetails.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            contactDetails.setAddress(request.getAddress());
        }
        profile.setContactDetails(contactDetails);

        return mapToDto(playerProfileRepository.save(profile));
    }

    public PlayerProfileResponse updatePreferences(Integer playerProfileId, PreferencesRequest request) {
        PlayerProfile profile = requireProfile(playerProfileId);

        Preferences preferences = profile.getPreferences() == null ? new Preferences() : profile.getPreferences();
        if (request.getLanguage() != null) {
            preferences.setLanguage(request.getLanguage());
        }
        if (request.getCurrency() != null) {
            preferences.setCurrency(request.getCurrency());
        }
        profile.setPreferences(preferences);

        return mapToDto(playerProfileRepository.save(profile));
    }

    /**
     * Lookup by auth account id. Empty when no profile row exists.
     */
    public Optional<PlayerProfileResponse> findByAccountIdOptional(String accountId) {
        return playerProfileRepository.findByAccountId(accountId).map(this::mapToDto);
    }

    public PlayerProfileResponse getByAccountId(String accountId) {
        return findByAccountIdOptional(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public void applyAccountStatus(String accountId, String authStatus) {
        if (accountId == null || authStatus == null) {
            return;
        }
        ProfileStatus next = switch (authStatus.toUpperCase()) {
            case "ACTIVE" -> ProfileStatus.ACTIVE;
            case "SUSPENDED" -> ProfileStatus.INACTIVE;
            default -> null;
        };
        if (next == null) {
            log.warn("Unknown auth status '{}' for accountId={}", authStatus, accountId);
            return;
        }
        playerProfileRepository.findByAccountId(accountId).ifPresentOrElse(profile -> {
            if (profile.getStatus() != next) {
                profile.setStatus(next);
                playerProfileRepository.save(profile);
                log.info("Profile {} for accountId={} → {} (from auth event)",
                        profile.getPlayerProfileId(), accountId, next);
            }
        }, () -> log.info("No profile yet for accountId={}; ignoring status={}", accountId, authStatus));
    }

    private PlayerProfile requireProfile(Integer playerProfileId) {
        return playerProfileRepository.findById(playerProfileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private PlayerProfileResponse mapToDto(PlayerProfile profile) {
        return PlayerProfileResponse.builder()
                .playerProfileId(profile.getPlayerProfileId())
                .accountId(profile.getAccountId())
                .fullName(profile.getFullName())
                .dateOfBirth(profile.getDateOfBirth())
                .status(profile.getStatus())
                .email(profile.getContactDetails() == null ? null : profile.getContactDetails().getEmail())
                .phone(profile.getContactDetails() == null ? null : profile.getContactDetails().getPhone())
                .address(profile.getContactDetails() == null ? null : profile.getContactDetails().getAddress())
                .language(profile.getPreferences() == null ? null : profile.getPreferences().getLanguage())
                .currency(profile.getPreferences() == null ? null : profile.getPreferences().getCurrency())
                .build();
    }
}
