package com.casino.profileservice.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casino.profileservice.users.model.PlayerProfile;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Integer> {

    Optional<PlayerProfile> findByAccountId(String accountId);

    boolean existsByAccountId(String accountId);

    boolean existsByAccountIdAndPlayerProfileIdNot(String accountId, Integer playerProfileId);
}
