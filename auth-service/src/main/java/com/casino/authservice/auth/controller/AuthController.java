package com.casino.authservice.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.casino.authservice.auth.dto.AccountResponse;
import com.casino.authservice.auth.dto.AccountStatusUpdateRequest;
import com.casino.authservice.auth.dto.AuthResponse;
import com.casino.authservice.auth.dto.LoginRequest;
import com.casino.authservice.auth.dto.RegisterRequest;
import com.casino.authservice.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public AccountResponse me(Authentication authentication) {
        return authService.me(authentication);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(Authentication authentication, @PathVariable("id") String accountId) {
        return authService.getAccount(authentication, accountId);
    }

    @PatchMapping("/accounts/{id}/status")
    public AccountResponse updateAccountStatus(@PathVariable("id") String accountId, @Valid @RequestBody AccountStatusUpdateRequest request) {
        return authService.updateStatus(accountId, request);
    }
}
