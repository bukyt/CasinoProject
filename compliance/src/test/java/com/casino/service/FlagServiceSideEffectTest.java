package com.casino.service;

import com.casino.dto.flag.CreateComplianceFlagDTO;
import com.casino.dto.flag.ModifyComplianceFlagDTO;
import com.casino.exceptions.flag.ComplianceFlagExistsException;
import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import com.casino.repository.ComplianceProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlagServiceSideEffectTest {

    @Mock
    private ComplianceFlagRepository complianceFlagRepository;

    @Mock
    private ComplianceProfileRepository complianceProfileRepository;

    @InjectMocks
    private FlagService flagService;

    @Test
    void createComplianceFlag_shouldUpdateProfileLastReviewDateAndRiskLevel() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile();

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.NO_AGE_VERIFICATION
        )).thenReturn(List.of());

        AtomicReference<ComplianceFlag> savedFlagRef = new AtomicReference<>();

        when(complianceFlagRepository.saveAndFlush(any(ComplianceFlag.class)))
            .thenAnswer(invocation -> {
                ComplianceFlag flag = invocation.getArgument(0);
                flag.setFlagId(10L);
                savedFlagRef.set(flag);
                return flag;
            });

        when(complianceFlagRepository.findByComplianceProfile_ComplianceId(profile.getComplianceId()))
            .thenAnswer(invocation -> List.of(savedFlagRef.get()));

        CreateComplianceFlagDTO request = new CreateComplianceFlagDTO(
            ComplianceFlagType.NO_AGE_VERIFICATION,
            ComplianceFlagSeverity.HIGH
        );

        flagService.createComplianceFlag(playerId, request);

        ComplianceFlag savedFlag = savedFlagRef.get();

        assertThat(savedFlag).isNotNull();
        assertThat(savedFlag.getComplianceProfile()).isSameAs(profile);
        assertThat(savedFlag.getType()).isEqualTo(ComplianceFlagType.NO_AGE_VERIFICATION);
        assertThat(savedFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.HIGH);
        assertThat(savedFlag.getResolvedDate()).isNull();

        assertThat(profile.getLastReviewDate()).isAfterOrEqualTo(before);
        assertThat(profile.getRiskLevel()).isEqualTo(ComplianceProfileRiskLevel.HIGH);

        verify(complianceFlagRepository).saveAndFlush(any(ComplianceFlag.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceFlag_toResolved_shouldUpdateProfileLastReviewDateAndRecalculateRiskLevel() {
        Long playerId = 123L;
        Long flagId = 10L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile();

        ComplianceFlag flag = new ComplianceFlag();
        flag.setFlagId(flagId);
        flag.setComplianceProfile(profile);
        flag.setType(ComplianceFlagType.NO_AGE_VERIFICATION);
        flag.setSeverity(ComplianceFlagSeverity.HIGH);
        flag.setCreatedDate(OffsetDateTime.now().minusDays(1));
        flag.setResolvedDate(null);

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByFlagIdAndComplianceProfile_ComplianceId(
            flagId,
            profile.getComplianceId()
        )).thenReturn(Optional.of(flag));

        when(complianceFlagRepository.saveAndFlush(any(ComplianceFlag.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceId(profile.getComplianceId()))
            .thenReturn(List.of(flag));

        ModifyComplianceFlagDTO request = new ModifyComplianceFlagDTO(
            null,
            ComplianceFlagSeverity.RESOLVED,
            null
        );

        flagService.modifyComplianceFlag(playerId, flagId, request);

        assertThat(flag.getSeverity()).isEqualTo(ComplianceFlagSeverity.RESOLVED);
        assertThat(flag.getResolvedDate()).isNotNull();
        assertThat(flag.getResolvedDate()).isAfterOrEqualTo(flag.getCreatedDate());

        assertThat(profile.getLastReviewDate()).isAfterOrEqualTo(before);
        assertThat(profile.getRiskLevel()).isEqualTo(ComplianceProfileRiskLevel.LOW);

        verify(complianceFlagRepository).saveAndFlush(flag);
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void createComplianceFlag_shouldRejectDuplicateUnresolvedType() {
        Long playerId = 123L;

        ComplianceProfile profile = profile();

        ComplianceFlag existingFlag = new ComplianceFlag();
        existingFlag.setFlagId(1L);
        existingFlag.setComplianceProfile(profile);
        existingFlag.setType(ComplianceFlagType.NO_AGE_VERIFICATION);
        existingFlag.setSeverity(ComplianceFlagSeverity.LOW);
        existingFlag.setCreatedDate(OffsetDateTime.now().minusDays(1));
        existingFlag.setResolvedDate(null);

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.NO_AGE_VERIFICATION
        )).thenReturn(List.of(existingFlag));

        CreateComplianceFlagDTO request = new CreateComplianceFlagDTO(
            ComplianceFlagType.NO_AGE_VERIFICATION,
            ComplianceFlagSeverity.HIGH
        );

        assertThatThrownBy(() -> flagService.createComplianceFlag(playerId, request))
            .isInstanceOf(ComplianceFlagExistsException.class);

        verify(complianceFlagRepository, never()).saveAndFlush(any());
        verify(complianceProfileRepository, never()).save(any());
    }

    private ComplianceProfile profile() {
        ComplianceProfile profile = new ComplianceProfile();
        profile.setComplianceId(1L);
        profile.setPlayerProfileId(123L);
        profile.setAgeVerified(true);
        profile.setSelfExcluded(false);
        profile.setRiskLevel(ComplianceProfileRiskLevel.LOW);
        profile.setLastReviewDate(OffsetDateTime.now().minusDays(5));
        return profile;
    }
}