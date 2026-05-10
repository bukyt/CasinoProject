package com.casino.repository;

import com.casino.model.limit.GamblingLimit;
import com.casino.model.limit.GamblingLimitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface GamblingLimitRepository extends JpaRepository<GamblingLimit, Long> {
    List<GamblingLimit> findByComplianceProfile_ComplianceIdOrderByCreatedDateDesc(
        Long complianceId
    );

    Optional<GamblingLimit> findByLimitIdAndComplianceProfile_ComplianceId(
        Long limitId,
        Long complianceId
    );

    List<GamblingLimit> findByComplianceProfile_ComplianceIdAndType(
        Long complianceId,
        GamblingLimitType type
    );

    @Query("""
        select l
        from GamblingLimit l
        where l.complianceProfile.complianceId = :complianceId
          and l.revokedDate is null
          and l.startDate <= :now
          and (l.endDate is null or l.endDate > :now)
        order by l.createdDate desc
        """)
    List<GamblingLimit> findActiveByComplianceId(
        Long complianceId,
        OffsetDateTime now
    );

}
