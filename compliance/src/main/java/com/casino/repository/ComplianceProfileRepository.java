package com.casino.repository;


import com.casino.model.profile.ComplianceProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComplianceProfileRepository extends JpaRepository<ComplianceProfile, Long> {

    List<ComplianceProfile> findByPlayerProfileId(Long playerProfileId);

    Optional<ComplianceProfile> findFirstByPlayerProfileId(Long playerProfileId);

}
