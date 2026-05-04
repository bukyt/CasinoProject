package com.casino.service;

import com.casino.dto.limit.CreateGamblingLimitDTO;
import com.casino.dto.limit.ModifyGamblingLimitDTO;
import com.casino.model.limit.GamblingLimit;
import com.casino.model.limit.GamblingLimitPeriod;
import com.casino.model.limit.GamblingLimitType;
import com.casino.model.profile.ComplianceProfile;
import com.casino.model.profile.ComplianceProfileRiskLevel;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimitServiceSideEffectTest {

    @Mock
    private GamblingLimitRepository gamblingLimitRepository;

    @Mock
    private ComplianceProfileRepository complianceProfileRepository;

    @InjectMocks
    private LimitService limitService;

    @Test
    void createComplianceLimit_shouldRevokeExistingActiveLimitOfSameTypeAndUpdateProfile() {
        Long playerId = 123L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile();

        GamblingLimit existingBetLimit = activeLimit(
            1L,
            profile,
            GamblingLimitType.BET,
            100
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(gamblingLimitRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            GamblingLimitType.BET
        )).thenReturn(List.of(existingBetLimit));

        AtomicReference<GamblingLimit> savedLimitRef = new AtomicReference<>();

        when(gamblingLimitRepository.save(any(GamblingLimit.class)))
            .thenAnswer(invocation -> {
                GamblingLimit limit = invocation.getArgument(0);
                limit.setLimitId(2L);
                savedLimitRef.set(limit);
                return limit;
            });

        CreateGamblingLimitDTO request = new CreateGamblingLimitDTO(
            GamblingLimitType.BET,
            250,
            GamblingLimitPeriod.DAILY,
            OffsetDateTime.now().minusHours(1),
            null
        );

        limitService.createComplianceLimit(playerId, request);

        assertThat(existingBetLimit.getRevokedDate()).isNotNull();
        assertThat(existingBetLimit.getRevokedDate()).isAfterOrEqualTo(before);

        GamblingLimit savedLimit = savedLimitRef.get();

        assertThat(savedLimit).isNotNull();
        assertThat(savedLimit.getComplianceProfile()).isSameAs(profile);
        assertThat(savedLimit.getType()).isEqualTo(GamblingLimitType.BET);
        assertThat(savedLimit.getAmount()).isEqualTo(250);
        assertThat(savedLimit.getRevokedDate()).isNull();

        assertThat(profile.getLastReviewDate()).isAfterOrEqualTo(before);

        verify(gamblingLimitRepository).saveAll(List.of(existingBetLimit));
        verify(gamblingLimitRepository).save(any(GamblingLimit.class));
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void createComplianceLimit_shouldNotRevokeDifferentTypeLimit() {
        Long playerId = 123L;

        ComplianceProfile profile = profile();

        GamblingLimit existingBetLimit = activeLimit(
            1L,
            profile,
            GamblingLimitType.BET,
            100
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(gamblingLimitRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            GamblingLimitType.WITHDRAWAL
        )).thenReturn(List.of());

        when(gamblingLimitRepository.save(any(GamblingLimit.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        CreateGamblingLimitDTO request = new CreateGamblingLimitDTO(
            GamblingLimitType.WITHDRAWAL,
            500,
            GamblingLimitPeriod.WEEKLY,
            OffsetDateTime.now().minusHours(1),
            null
        );

        limitService.createComplianceLimit(playerId, request);

        assertThat(existingBetLimit.getRevokedDate()).isNull();

        verify(gamblingLimitRepository).saveAll(List.of());
        verify(complianceProfileRepository).save(profile);
    }

    @Test
    void modifyComplianceLimit_shouldRevokeOtherActiveLimitOfNewTypeAndUpdateProfile() {
        Long playerId = 123L;
        Long limitId = 1L;
        OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

        ComplianceProfile profile = profile();

        GamblingLimit limitBeingModified = activeLimit(
            limitId,
            profile,
            GamblingLimitType.WITHDRAWAL,
            500
        );

        GamblingLimit otherActiveBetLimit = activeLimit(
            2L,
            profile,
            GamblingLimitType.BET,
            100
        );

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(gamblingLimitRepository.findByLimitIdAndComplianceProfile_ComplianceId(
            limitId,
            profile.getComplianceId()
        )).thenReturn(Optional.of(limitBeingModified));

        when(gamblingLimitRepository.findByComplianceProfile_ComplianceIdAndType(
            profile.getComplianceId(),
            GamblingLimitType.BET
        )).thenReturn(List.of(otherActiveBetLimit));

        when(gamblingLimitRepository.save(any(GamblingLimit.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ModifyGamblingLimitDTO request = new ModifyGamblingLimitDTO(
            GamblingLimitType.BET,
            300,
            GamblingLimitPeriod.DAILY,
            null,
            null,
            null
        );

        limitService.modifyComplianceLimit(playerId, limitId, request);

        assertThat(otherActiveBetLimit.getRevokedDate()).isNotNull();
        assertThat(otherActiveBetLimit.getRevokedDate()).isAfterOrEqualTo(before);

        assertThat(limitBeingModified.getType()).isEqualTo(GamblingLimitType.BET);
        assertThat(limitBeingModified.getAmount()).isEqualTo(300);
        assertThat(limitBeingModified.getPeriod()).isEqualTo(GamblingLimitPeriod.DAILY);

        assertThat(profile.getLastReviewDate()).isAfterOrEqualTo(before);

        verify(gamblingLimitRepository).saveAll(List.of(otherActiveBetLimit));
        verify(gamblingLimitRepository).save(limitBeingModified);
        verify(complianceProfileRepository).save(profile);
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

    private GamblingLimit activeLimit(
        Long limitId,
        ComplianceProfile profile,
        GamblingLimitType type,
        int amount
    ) {
        GamblingLimit limit = new GamblingLimit();
        limit.setLimitId(limitId);
        limit.setComplianceProfile(profile);
        limit.setType(type);
        limit.setAmount(amount);
        limit.setPeriod(GamblingLimitPeriod.DAILY);
        limit.setCreatedDate(OffsetDateTime.now().minusDays(1));
        limit.setStartDate(OffsetDateTime.now().minusHours(2));
        limit.setEndDate(null);
        limit.setRevokedDate(null);
        return limit;
    }
}