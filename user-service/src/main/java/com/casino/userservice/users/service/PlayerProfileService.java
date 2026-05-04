package com.casino.userservice.users.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.casino.userservice.users.dto.ContactDetailsRequest;
import com.casino.userservice.users.dto.PlayerProfileRequest;
import com.casino.userservice.users.dto.PlayerProfileResponse;
import com.casino.userservice.users.dto.PreferencesRequest;
import com.casino.userservice.users.model.ContactDetails;
import com.casino.userservice.users.model.PlayerProfile;
import com.casino.userservice.users.model.Preferences;
import com.casino.userservice.users.repository.PlayerProfileRepository;

import org.springframework.http.HttpStatus;

@Service
public class PlayerProfileService {
    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    public PlayerProfileResponse createProfile(PlayerProfileRequest request) {
        if (playerProfileRepository.existsByAccountId(request.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Profile already exists for accountId: " + request.getAccountId());
        }

        PlayerProfile profile = PlayerProfile.builder()
                .playerProfileId(UUID.randomUUID().toString())
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

    public PlayerProfileResponse getProfile(String id) {
        return mapToDto(playerProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found")));
    }

    public PlayerProfileResponse updateProfile(String id, PlayerProfileRequest request) {
        PlayerProfile profile = playerProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (playerProfileRepository.existsByAccountIdAndPlayerProfileIdNot(request.getAccountId(), id)) {
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

    public PlayerProfileResponse updateContact(String id, ContactDetailsRequest request) {
        PlayerProfile profile = playerProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

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

    public PlayerProfileResponse updatePreferences(String id, PreferencesRequest request) {
        PlayerProfile profile = playerProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

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

    public PlayerProfileResponse getByAccountId(String accountId) {
        return mapToDto(playerProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found")));
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
