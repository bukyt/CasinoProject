package com.casino.authservice.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casino.authservice.auth.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);
}
