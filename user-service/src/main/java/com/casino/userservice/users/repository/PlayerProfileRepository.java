package com.casino.userservice.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casino.userservice.users.model.PlayerProfile;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, String> {
    Optional<PlayerProfile> findByAccountId(String accountId);

    // Returns true if ANY profile already uses this accountId.
    // Used on create to enforce one profile per account.
    boolean existsByAccountId(String accountId);

    // Returns true if another profile (different id) already uses this accountId.
    // Used on update so the same profile can keep its own accountId,
    // but cannot take an accountId that belongs to a different profile.
    boolean existsByAccountIdAndPlayerProfileIdNot(String accountId, String playerProfileId);
}
