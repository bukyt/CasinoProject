package com.casino.repository;

import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceFlagRepository extends JpaRepository<ComplianceFlag, Long> {

    Optional<ComplianceFlag> findByFlagIdAndComplianceProfile_ComplianceId(
        Long flagId,
        Long complianceId
    );

    List<ComplianceFlag> findByComplianceProfile_ComplianceIdAndType(
        Long complianceId,
        ComplianceFlagType type
    );

    List<ComplianceFlag> findByComplianceProfile_ComplianceId(
        Long complianceId
    );
}
