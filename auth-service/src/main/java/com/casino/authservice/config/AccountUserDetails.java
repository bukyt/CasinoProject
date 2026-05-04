package com.casino.authservice.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.casino.authservice.auth.model.Account;
import com.casino.authservice.auth.model.AccountStatus;
import com.casino.authservice.auth.model.RoleAssignment;

import lombok.Getter;

@Getter
public class AccountUserDetails implements UserDetails {

    private final Account account;
    private final List<RoleAssignment> roleAssignments;

    public AccountUserDetails(Account account, List<RoleAssignment> roleAssignments) {
        this.account = account;
        this.roleAssignments = roleAssignments;
    }

    public String getRolesAsCommaSeparated() {
        return roleAssignments.stream()
                .map(r -> r.getRoleName().name())
                .collect(Collectors.joining(","));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleAssignments.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.getStatus() == AccountStatus.ACTIVE;
    }
}
