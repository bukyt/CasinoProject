package com.casino.service;

import com.casino.dto.profile.ComplianceProfileDto;
import com.casino.dto.profile.ModifyComplianceProfileDTO;
import com.casino.exceptions.profile.ComplianceProfileExistsException;
import com.casino.exceptions.profile.ComplianceProfileMissingException;
import com.casino.model.flag.ComplianceFlag;
import com.casino.model.flag.ComplianceFlagSeverity;
import com.casino.model.flag.ComplianceFlagType;
import com.casino.model.limit.GamblingLimit;
import com.casino.model.limit.GamblingLimitPeriod;
import com.casino.model.limit.GamblingLimitType;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
import com.casino.repository.ComplianceFlagRepository;
import com.casino.repository.ComplianceProfileRepository;
import com.casino.repository.GamblingLimitRepository;
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
class ComplianceServiceTest {

    @Mock
    private ComplianceProfileRepository complianceProfileRepository;

    @Mock
    private ComplianceFlagRepository complianceFlagRepository;

    @Mock
    private GamblingLimitRepository gamblingLimitRepository;

    @InjectMocks
    private ComplianceService complianceService;

    @Test
    void createComplianceProfile_shouldCreateProfileWithDefaults() {
        Long playerId = 123L;

        when(complianceProfileRepository.findByPlayerProfileId(playerId))
            .thenReturn(List.of());

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> {
                ComplianceProfile profile = invocation.getArgument(0);
                profile.setComplianceId(1L);
                return profile;
            });

        ComplianceProfileDto response = complianceService.createComplianceProfile(playerId);

        assertThat(response.complianceId()).isEqualTo(1L);
        assertThat(response.playerProfileId()).isEqualTo(playerId);
        assertThat(response.ageVerified()).isFalse();
        assertThat(response.selfExcluded()).isFalse();
        assertThat(response.riskLevel()).isEqualTo(ComplianceProfileRiskLevel.UNASSESSED);
        assertThat(response.lastReviewDate()).isNotNull();
        assertThat(response.flags()).isEmpty();
        assertThat(response.limits()).isEmpty();

        verify(complianceProfileRepository).save(any(ComplianceProfile.class));
        verifyNoInteractions(complianceFlagRepository);
    }

    @Test
    void createComplianceProfile_shouldThrow_whenProfileAlreadyExists() {
        Long playerId = 123L;

        ComplianceProfile existingProfile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        when(complianceProfileRepository.findByPlayerProfileId(playerId))
            .thenReturn(List.of(existingProfile));

        assertThatThrownBy(() -> complianceService.createComplianceProfile(playerId))
            .isInstanceOf(ComplianceProfileExistsException.class);

        verify(complianceProfileRepository, never()).save(any());
    }

    @Test
    void getComplianceProfile_shouldReturnDtoWithFlagsAndLimits() {
        Long playerId = 123L;

        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        ComplianceFlag flag = unresolvedFlag(
            profile,
            ComplianceFlagType.NO_AGE_VERIFICATION,
            ComplianceFlagSeverity.LOW
        );

        GamblingLimit limit = activeLimit(
            profile,
            GamblingLimitType.BET,
            100
        );

        profile.setFlags(List.of(flag));
        profile.setLimits(List.of(limit));

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        ComplianceProfileDto response = complianceService.getComplianceProfile(playerId);

        assertThat(response.playerProfileId()).isEqualTo(playerId);

        assertThat(response.flags()).hasSize(1);
        assertThat(response.flags().get(0).type()).isEqualTo(ComplianceFlagType.NO_AGE_VERIFICATION);
        assertThat(response.flags().get(0).severity()).isEqualTo(ComplianceFlagSeverity.LOW);

        assertThat(response.limits()).hasSize(1);
        assertThat(response.limits().get(0).type()).isEqualTo(GamblingLimitType.BET);
        assertThat(response.limits().get(0).amount()).isEqualTo(100);
    }

    private GamblingLimit activeLimit(
        ComplianceProfile profile,
        GamblingLimitType type,
        int amount
    ) {
        GamblingLimit limit = new GamblingLimit();
        limit.setLimitId(10L);
        limit.setComplianceProfile(profile);
        limit.setType(type);
        limit.setAmount(amount);
        limit.setPeriod(GamblingLimitPeriod.DAILY);
        limit.setCreatedDate(OffsetDateTime.now().minusDays(1));
        limit.setStartDate(OffsetDateTime.now().minusHours(1));
        limit.setEndDate(null);
        limit.setRevokedDate(null);
        return limit;
    }

    @Test
    void getComplianceProfile_shouldThrow_whenProfileIsMissing() {
        Long playerId = 999L;

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> complianceService.getComplianceProfile(playerId))
            .isInstanceOf(ComplianceProfileMissingException.class);
    }

    @Test
    void modifyComplianceProfile_shouldUpdateAgeVerifiedAndLastReviewDate() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile(playerId, false, false, ComplianceProfileRiskLevel.LOW);

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            true,
            null,
            null
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        assertThat(response.ageVerified()).isTrue();
        assertThat(response.lastReviewDate()).isAfterOrEqualTo(before);

        verify(complianceFlagRepository, never()).save(any());
        verify(complianceFlagRepository, never()).saveAll(any());
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldCreateSelfExclusionFlag_whenSelfExcludedIsSetToTrue() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.SELF_EXCLUSION
        )).thenReturn(List.of());

        AtomicReference<ComplianceFlag> savedFlagRef = new AtomicReference<>();

        when(complianceFlagRepository.save(any(ComplianceFlag.class)))
            .thenAnswer(invocation -> {
                ComplianceFlag flag = invocation.getArgument(0);
                flag.setFlagId(10L);
                savedFlagRef.set(flag);
                return flag;
            });

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            null,
            true,
            null
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        ComplianceFlag savedFlag = savedFlagRef.get();

        assertThat(response.selfExcluded()).isTrue();
        assertThat(response.lastReviewDate()).isAfterOrEqualTo(before);

        assertThat(savedFlag).isNotNull();
        assertThat(savedFlag.getComplianceProfile()).isSameAs(profile);
        assertThat(savedFlag.getType()).isEqualTo(ComplianceFlagType.SELF_EXCLUSION);
        assertThat(savedFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.HIGH);
        assertThat(savedFlag.getResolvedDate()).isNull();

        verify(complianceFlagRepository).save(any(ComplianceFlag.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldNotCreateDuplicateSelfExclusionFlag_whenUnresolvedSelfExclusionFlagAlreadyExists() {
        Long playerId = 123L;

        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        ComplianceFlag existingSelfExclusionFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.SELF_EXCLUSION,
            ComplianceFlagSeverity.HIGH
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.SELF_EXCLUSION
        )).thenReturn(List.of(existingSelfExclusionFlag));

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            null,
            true,
            null
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        assertThat(response.selfExcluded()).isTrue();

        verify(complianceFlagRepository, never()).save(any(ComplianceFlag.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldResolveSelfExclusionFlag_whenSelfExcludedIsSetToFalse() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile(playerId, true, true, ComplianceProfileRiskLevel.HIGH);

        ComplianceFlag selfExclusionFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.SELF_EXCLUSION,
            ComplianceFlagSeverity.HIGH
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.SELF_EXCLUSION
        )).thenReturn(List.of(selfExclusionFlag));

        when(complianceFlagRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            null,
            false,
            null
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        assertThat(response.selfExcluded()).isFalse();
        assertThat(response.lastReviewDate()).isAfterOrEqualTo(before);

        assertThat(selfExclusionFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.RESOLVED_ADMIN);
        assertThat(selfExclusionFlag.getResolvedDate()).isNotNull();
        assertThat(selfExclusionFlag.getResolvedDate()).isAfterOrEqualTo(before);

        verify(complianceFlagRepository).saveAll(any());
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldCreateManualRiskLevelFlag_whenRiskLevelIsChanged() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.MANUAL_RISK_LEVEL
        )).thenReturn(List.of());

        AtomicReference<ComplianceFlag> savedFlagRef = new AtomicReference<>();

        when(complianceFlagRepository.save(any(ComplianceFlag.class)))
            .thenAnswer(invocation -> {
                ComplianceFlag flag = invocation.getArgument(0);
                flag.setFlagId(20L);
                savedFlagRef.set(flag);
                return flag;
            });

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            null,
            null,
            ComplianceProfileRiskLevel.HIGH
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        ComplianceFlag savedFlag = savedFlagRef.get();

        assertThat(response.riskLevel()).isEqualTo(ComplianceProfileRiskLevel.HIGH);
        assertThat(response.lastReviewDate()).isAfterOrEqualTo(before);

        assertThat(savedFlag).isNotNull();
        assertThat(savedFlag.getComplianceProfile()).isSameAs(profile);
        assertThat(savedFlag.getType()).isEqualTo(ComplianceFlagType.MANUAL_RISK_LEVEL);
        assertThat(savedFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.HIGH);
        assertThat(savedFlag.getResolvedDate()).isNull();

        verify(complianceFlagRepository).save(any(ComplianceFlag.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldResolveExistingManualRiskFlagBeforeCreatingNewOne() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.HIGH);

        ComplianceFlag existingManualRiskFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.MANUAL_RISK_LEVEL,
            ComplianceFlagSeverity.HIGH
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(complianceFlagRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            ComplianceFlagType.MANUAL_RISK_LEVEL
        )).thenReturn(List.of(existingManualRiskFlag));

        when(complianceFlagRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        AtomicReference<ComplianceFlag> savedFlagRef = new AtomicReference<>();

        when(complianceFlagRepository.save(any(ComplianceFlag.class)))
            .thenAnswer(invocation -> {
                ComplianceFlag flag = invocation.getArgument(0);
                flag.setFlagId(30L);
                savedFlagRef.set(flag);
                return flag;
            });

        when(complianceProfileRepository.save(any(ComplianceProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));


        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            null,
            null,
            ComplianceProfileRiskLevel.LOW
        );

        ComplianceProfileDto response = complianceService.modifyComplianceProfile(playerId, request);

        ComplianceFlag newFlag = savedFlagRef.get();

        assertThat(response.riskLevel()).isEqualTo(ComplianceProfileRiskLevel.LOW);

        assertThat(existingManualRiskFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.RESOLVED_ADMIN);
        assertThat(existingManualRiskFlag.getResolvedDate()).isNotNull();
        assertThat(existingManualRiskFlag.getResolvedDate()).isAfterOrEqualTo(before);

        assertThat(newFlag).isNotNull();
        assertThat(newFlag.getType()).isEqualTo(ComplianceFlagType.MANUAL_RISK_LEVEL);
        assertThat(newFlag.getSeverity()).isEqualTo(ComplianceFlagSeverity.LOW);
        assertThat(newFlag.getResolvedDate()).isNull();

        verify(complianceFlagRepository).saveAll(any());
        verify(complianceFlagRepository).save(any(ComplianceFlag.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceProfile_shouldThrow_whenProfileIsMissing() {
        Long playerId = 999L;

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.empty());

        ModifyComplianceProfileDTO request = new ModifyComplianceProfileDTO(
            true,
            null,
            null
        );

        assertThatThrownBy(() -> complianceService.modifyComplianceProfile(playerId, request))
            .isInstanceOf(ComplianceProfileMissingException.class);

        verify(complianceProfileRepository, never()).save(any());
        verify(complianceFlagRepository, never()).save(any());
    }

    private ComplianceProfile profile(
        Long playerProfileId,
        boolean ageVerified,
        boolean selfExcluded,
        ComplianceProfileRiskLevel riskLevel
    ) {
        ComplianceProfile profile = new ComplianceProfile();
        profile.setComplianceId(1L);
        profile.setPlayerProfileId(playerProfileId);
        profile.setAgeVerified(ageVerified);
        profile.setSelfExcluded(selfExcluded);
        profile.setRiskLevel(riskLevel);
        profile.setLastReviewDate(OffsetDateTime.now().minusDays(1));
        return profile;
    }

    private ComplianceFlag unresolvedFlag(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity
    ) {
        ComplianceFlag flag = new ComplianceFlag();
        flag.setFlagId(10L);
        flag.setComplianceProfile(profile);
        flag.setType(type);
        flag.setSeverity(severity);
        flag.setCreatedDate(OffsetDateTime.now().minusDays(1));
        flag.setResolvedDate(null);
        return flag;
    }
}