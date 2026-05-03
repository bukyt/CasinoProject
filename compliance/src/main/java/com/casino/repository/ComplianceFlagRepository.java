package com.casino.repository;

import com.casino.model.ComplianceFlag;
import com.casino.model.ComplianceFlagSeverity;
import com.casino.model.ComplianceFlagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceFlagRepository extends JpaRepository<ComplianceFlag, Long> {
    Optional<ComplianceFlag> findByFlagIdAndComplianceId(Long flagId, Long complianceId);

    List<ComplianceFlag> findByComplianceIdAndTypeAndSeverity(
        Long complianceId, ComplianceFlagType type, ComplianceFlagSeverity severity);
}
