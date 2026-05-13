package com.casino.authservice.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.casino.authservice.auth.dto.AccountResponse;
import com.casino.authservice.auth.dto.AccountStatusUpdateRequest;
import com.casino.authservice.auth.dto.AuthResponse;
import com.casino.authservice.auth.dto.LoginRequest;
import com.casino.authservice.auth.dto.RegisterRequest;
import com.casino.authservice.auth.model.Account;
import com.casino.authservice.auth.model.AccountStatus;
import com.casino.authservice.auth.model.RoleAssignment;
import com.casino.authservice.auth.model.RoleName;
import com.casino.authservice.auth.repository.AccountRepository;
import com.casino.authservice.auth.repository.RoleAssignmentRepository;
import com.casino.authservice.config.AccountUserDetails;
import com.casino.authservice.events.AccountEventPublisher;
import com.casino.authservice.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountEventPublisher accountEventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this username already exists");
        }
        String accountId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Account account = Account.builder()
                .accountId(accountId)
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(AccountStatus.ACTIVE)
                .createdDate(now)
                .build();
        accountRepository.save(account);
        RoleAssignment playerRole = RoleAssignment.builder()
                .roleId(UUID.randomUUID().toString())
                .accountId(accountId)
                .roleName(RoleName.PLAYER)
                .build();
        roleAssignmentRepository.save(playerRole);
        String token = jwtService.generateToken(account.getUsername(), accountId, RoleName.PLAYER.name());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            AccountUserDetails details = (AccountUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(
                    details.getUsername(),
                    details.getAccount().getAccountId(),
                    details.getRolesAsCommaSeparated());
            return new AuthResponse(token);
        } catch (DisabledException | LockedException e) {
            // AccountUserDetails#isEnabled() returns false for non-ACTIVE accounts,
            // so Spring's pre-auth check throws DisabledException before the password is verified.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account is suspended. Please contact support.");
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed");
        }
    }

    public AccountResponse me(Authentication authentication) {
        AccountUserDetails details = (AccountUserDetails) authentication.getPrincipal();
        return toResponse(details.getAccount());
    }

    public AccountResponse getAccount(Authentication authentication, String accountId) {
        AccountUserDetails details = (AccountUserDetails) authentication.getPrincipal();
        boolean isAdmin = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!isAdmin && !details.getAccount().getAccountId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view this account");
        }
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return toResponse(account);
    }

    @Transactional
    public AccountResponse updateStatus(String accountId, AccountStatusUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        account.setStatus(request.getStatus());
        accountRepository.save(account);
        // Notify other services asynchronously
        accountEventPublisher.publishStatusChanged(account.getAccountId(), account.getStatus());
        return toResponse(account);
    }

    public void logout(String rawToken) {
        jwtService.blacklistToken(rawToken);
    }

    private AccountResponse toResponse(Account account) {
        List<RoleAssignment> roles = roleAssignmentRepository.findByAccountId(account.getAccountId());
        List<String> roleNames = roles.stream().map(r -> r.getRoleName().name()).toList();
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .status(account.getStatus())
                .createdDate(account.getCreatedDate())
                .roles(roleNames)
                .build();
    }
}
