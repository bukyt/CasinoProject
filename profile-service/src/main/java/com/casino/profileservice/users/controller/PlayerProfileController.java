package com.casino.profileservice.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpHeaders;

import com.casino.profileservice.users.dto.ContactDetailsRequest;
import com.casino.profileservice.users.dto.PlayerProfileRequest;
import com.casino.profileservice.users.dto.PlayerProfileResponse;
import com.casino.profileservice.users.dto.PreferencesRequest;
import com.casino.profileservice.users.service.PlayerProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profiles")
public class PlayerProfileController {
    @Autowired
    private PlayerProfileService playerProfileService;

    @PostMapping
    public PlayerProfileResponse createProfile(@Valid @RequestBody PlayerProfileRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return playerProfileService.createProfile(request, authorization);
    }

    @GetMapping("/{playerProfileId}")
    public PlayerProfileResponse getProfile(@PathVariable Integer playerProfileId) {
        return playerProfileService.getProfile(playerProfileId);
    }

    @PutMapping("/{playerProfileId}")
    public PlayerProfileResponse updateProfile(@PathVariable Integer playerProfileId,
            @Valid @RequestBody PlayerProfileRequest request) {
        return playerProfileService.updateProfile(playerProfileId, request);
    }

    @PatchMapping("/{playerProfileId}/contact")
    public PlayerProfileResponse updateContact(@PathVariable Integer playerProfileId,
            @Valid @RequestBody ContactDetailsRequest request) {
        return playerProfileService.updateContact(playerProfileId, request);
    }

    @PatchMapping("/{playerProfileId}/preferences")
    public PlayerProfileResponse updatePreferences(@PathVariable Integer playerProfileId,
            @Valid @RequestBody PreferencesRequest request) {
        return playerProfileService.updatePreferences(playerProfileId, request);
    }

    @GetMapping("/account/{accountId}")
    public PlayerProfileResponse getByAccountId(@PathVariable String accountId) {
        return playerProfileService.getByAccountId(accountId);
    }
}
