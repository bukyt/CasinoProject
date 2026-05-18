package com.casino.profileservice.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.casino.profileservice.users.model.PlayerProfile;

@Component
public class ProfileAuth {

    public void requireOwnerOrAdmin(PlayerProfile profile) {
        if (isAdmin()) {
            return;
        }
        if (!callerAccountId().equals(profile.getAccountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this profile");
        }
    }

    public void requireAccountAccess(String accountId) {
        if (isAdmin()) {
            return;
        }
        if (!callerAccountId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to access this account");
        }
    }

    public void requireOwnAccount(String accountId) {
        if (!callerAccountId().equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Profile accountId must match your account");
        }
    }

    private static boolean isAdmin() {
        Authentication auth = requireAuth();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private static String callerAccountId() {
        return String.valueOf(requireAuth().getPrincipal());
    }

    private static Authentication requireAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return auth;
    }
}
