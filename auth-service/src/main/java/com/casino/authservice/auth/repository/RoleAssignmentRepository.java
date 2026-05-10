package com.casino.authservice.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.casino.authservice.auth.model.RoleAssignment;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, String> {

    List<RoleAssignment> findByAccountId(String accountId);
}
