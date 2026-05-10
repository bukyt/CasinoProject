package com.casino.authservice.config;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.casino.authservice.auth.model.Account;
import com.casino.authservice.auth.model.RoleAssignment;
import com.casino.authservice.auth.repository.AccountRepository;
import com.casino.authservice.auth.repository.RoleAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        List<RoleAssignment> roles = roleAssignmentRepository.findByAccountId(account.getAccountId());
        if (roles.isEmpty()) {
            throw new UsernameNotFoundException("No roles for account: " + username);
        }
        return new AccountUserDetails(account, roles);
    }
}
