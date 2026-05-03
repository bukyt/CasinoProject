package com.casino.repository;

import com.casino.model.GamblingLimit;
import com.casino.model.GamblingLimitPeriod;
import com.casino.model.GamblingLimitType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GamblingLimitRepository extends JpaRepository<GamblingLimit, Long> {
    Optional<GamblingLimit> findByComplianceIdAndTypeAndPeriod(
            Long complianceId, GamblingLimitType type, GamblingLimitPeriod period);

    Optional<GamblingLimit> findByLimitIdAndComplianceId(Long limitId, Long complianceId);

    List<GamblingLimit> findByComplianceIdOrderByCreatedDateDesc(
            Long complianceId
    );

}
