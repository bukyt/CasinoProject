package com.casino.authservice;

import java.time.Instant;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.casino.authservice.auth.model.Account;
import com.casino.authservice.auth.model.AccountStatus;
import com.casino.authservice.auth.model.RoleAssignment;
import com.casino.authservice.auth.model.RoleName;
import com.casino.authservice.auth.repository.AccountRepository;
import com.casino.authservice.auth.repository.RoleAssignmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (accountRepository.existsByUsername("admin")) {
            return;
        }
        String accountId = UUID.randomUUID().toString();
        Account admin = Account.builder()
                .accountId(accountId)
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .status(AccountStatus.ACTIVE)
                .createdDate(Instant.now())
                .build();
        accountRepository.save(admin);
        roleAssignmentRepository.save(RoleAssignment.builder()
                .roleId(UUID.randomUUID().toString())
                .accountId(accountId)
                .roleName(RoleName.ADMIN)
                .build());
        roleAssignmentRepository.save(RoleAssignment.builder()
                .roleId(UUID.randomUUID().toString())
                .accountId(accountId)
                .roleName(RoleName.PLAYER)
                .build());
        log.info("Seeded default admin user (username: admin). Change password in production.");
    }
}
