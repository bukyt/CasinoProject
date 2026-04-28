package com.casino.userservice.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.casino.userservice.users.dto.ContactDetailsRequest;
import com.casino.userservice.users.dto.PlayerProfileRequest;
import com.casino.userservice.users.dto.PlayerProfileResponse;
import com.casino.userservice.users.dto.PreferencesRequest;
import com.casino.userservice.users.service.PlayerProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/profiles")
public class PlayerProfileController {
    @Autowired
    private PlayerProfileService playerProfileService;

    @PostMapping
    public PlayerProfileResponse createProfile(@Valid @RequestBody PlayerProfileRequest request) {
        return playerProfileService.createProfile(request);
    }

    @GetMapping("/{id}")
    public PlayerProfileResponse getProfile(@PathVariable String id) {
        return playerProfileService.getProfile(id);
    }

    @PutMapping("/{id}")
    public PlayerProfileResponse updateProfile(@PathVariable String id, @Valid @RequestBody PlayerProfileRequest request) {
        return playerProfileService.updateProfile(id, request);
    }

    @PatchMapping("/{id}/contact")
    public PlayerProfileResponse updateContact(@PathVariable String id, @Valid @RequestBody ContactDetailsRequest request) {
        return playerProfileService.updateContact(id, request);
    }

    @PatchMapping("/{id}/preferences")
    public PlayerProfileResponse updatePreferences(@PathVariable String id, @RequestBody PreferencesRequest request) {
        return playerProfileService.updatePreferences(id, request);
    }

    @GetMapping("/account/{accountId}")
    public PlayerProfileResponse getByAccountId(@PathVariable String accountId) {
        return playerProfileService.getByAccountId(accountId);
    }
}
