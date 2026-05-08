package com.casino.service;

import com.casino.dto.profile.EligibilityResponseDTO;
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

import static com.casino.dto.profile.EligibilityBlockReason.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {

    @Mock
    private ComplianceProfileRepository complianceProfileRepository;

    @Mock
    private GamblingLimitRepository gamblingLimitRepository;

    @Mock
    private ComplianceFlagRepository complianceFlagRepository;

    @InjectMocks
    private EligibilityService eligibilityService;

    @Test
    void checkEligibility_shouldAllowBetAndWithdraw_whenProfileIsCleanAndLimitsArePositive() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        GamblingLimit betLimit = activeLimit(profile, GamblingLimitType.BET, 100);
        GamblingLimit withdrawalLimit = activeLimit(profile, GamblingLimitType.WITHDRAWAL, 500);

        mockEligibilityData(playerId, profile, List.of(betLimit, withdrawalLimit), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.playerProfileId()).isEqualTo(playerId);
        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isTrue();
        assertThat(response.blockReasons()).isEmpty();

        assertThat(response.activeBetLimit()).isNotNull();
        assertThat(response.activeBetLimit().amount()).isEqualTo(100);
        assertThat(response.activeBetLimit().period()).isEqualTo(GamblingLimitPeriod.DAILY);

        assertThat(response.activeWithdrawalLimit()).isNotNull();
        assertThat(response.activeWithdrawalLimit().amount()).isEqualTo(500);
        assertThat(response.activeWithdrawalLimit().period()).isEqualTo(GamblingLimitPeriod.DAILY);
    }

    @Test
    void checkEligibility_shouldAllowBetAndWithdraw_whenNoActiveLimitsExist() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isTrue();
        assertThat(response.blockReasons()).isEmpty();
        assertThat(response.activeBetLimit()).isNull();
        assertThat(response.activeWithdrawalLimit()).isNull();
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenAgeIsNotVerified() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, false, false, ComplianceProfileRiskLevel.LOW);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(AGE_NOT_VERIFIED);
    }

    @Test
    void checkEligibility_shouldBlockBetOnly_whenPlayerIsSelfExcluded() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, true, ComplianceProfileRiskLevel.LOW);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isTrue();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(SELF_EXCLUDED);
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenRiskLevelIsUnassessed() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.UNASSESSED);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(AML_REVIEW_REQUIRED);
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenRiskLevelIsHigh() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.HIGH);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(
            HIGH_RISK_PROFILE,
            AML_REVIEW_REQUIRED
        );
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenRiskLevelIsCritical() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.CRITICAL);

        mockEligibilityData(playerId, profile, List.of(), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(
            CRITICAL_RISK_PROFILE,
            AML_REVIEW_REQUIRED
        );
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenUnresolvedHighFlagExists() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        ComplianceFlag highFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.MANUAL_RISK_LEVEL,
            ComplianceFlagSeverity.HIGH
        );

        mockEligibilityData(playerId, profile, List.of(), List.of(highFlag));

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(
            HIGH_RISK_PROFILE,
            AML_REVIEW_REQUIRED
        );
    }

    @Test
    void checkEligibility_shouldBlockBetAndWithdraw_whenUnresolvedMediumFlagExists() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        ComplianceFlag mediumFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.NO_AGE_VERIFICATION,
            ComplianceFlagSeverity.MEDIUM
        );

        mockEligibilityData(playerId, profile, List.of(), List.of(mediumFlag));

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(
            AML_REVIEW_REQUIRED
        );
    }

    @Test
    void checkEligibility_shouldIgnoreResolvedHighFlag() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        ComplianceFlag resolvedHighFlag = resolvedFlag(
            profile,
            ComplianceFlagType.MANUAL_RISK_LEVEL,
            ComplianceFlagSeverity.RESOLVED_ADMIN
        );

        mockEligibilityData(playerId, profile, List.of(), List.of(resolvedHighFlag));

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isTrue();
        assertThat(response.blockReasons()).isEmpty();
    }

    @Test
    void checkEligibility_shouldBlockBetOnly_whenActiveBetLimitIsZero() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        GamblingLimit zeroBetLimit = activeLimit(profile, GamblingLimitType.BET, 0);
        GamblingLimit withdrawalLimit = activeLimit(profile, GamblingLimitType.WITHDRAWAL, 500);

        mockEligibilityData(playerId, profile, List.of(zeroBetLimit, withdrawalLimit), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isTrue();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(BET_LIMIT_ZERO);

        assertThat(response.activeBetLimit()).isNotNull();
        assertThat(response.activeBetLimit().amount()).isZero();
        assertThat(response.activeBetLimit().period()).isEqualTo(GamblingLimitPeriod.DAILY);

        assertThat(response.activeWithdrawalLimit()).isNotNull();
        assertThat(response.activeWithdrawalLimit().amount()).isEqualTo(500);
    }

    @Test
    void checkEligibility_shouldBlockWithdrawOnly_whenActiveWithdrawalLimitIsZero() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        GamblingLimit betLimit = activeLimit(profile, GamblingLimitType.BET, 100);
        GamblingLimit zeroWithdrawalLimit = activeLimit(profile, GamblingLimitType.WITHDRAWAL, 0);

        mockEligibilityData(playerId, profile, List.of(betLimit, zeroWithdrawalLimit), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(WITHDRAWAL_LIMIT_ZERO);

        assertThat(response.activeBetLimit()).isNotNull();
        assertThat(response.activeBetLimit().amount()).isEqualTo(100);

        assertThat(response.activeWithdrawalLimit()).isNotNull();
        assertThat(response.activeWithdrawalLimit().amount()).isZero();
        assertThat(response.activeWithdrawalLimit().period()).isEqualTo(GamblingLimitPeriod.DAILY);
    }

    @Test
    void checkEligibility_shouldReturnOnlyBetLimit_whenOnlyBetLimitIsActive() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        GamblingLimit betLimit = activeLimit(profile, GamblingLimitType.BET, 100);

        mockEligibilityData(playerId, profile, List.of(betLimit), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isTrue();

        assertThat(response.activeBetLimit()).isNotNull();
        assertThat(response.activeBetLimit().amount()).isEqualTo(100);

        assertThat(response.activeWithdrawalLimit()).isNull();
    }

    @Test
    void checkEligibility_shouldReturnOnlyWithdrawalLimit_whenOnlyWithdrawalLimitIsActive() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, true, false, ComplianceProfileRiskLevel.LOW);

        GamblingLimit withdrawalLimit = activeLimit(profile, GamblingLimitType.WITHDRAWAL, 500);

        mockEligibilityData(playerId, profile, List.of(withdrawalLimit), List.of());

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isTrue();
        assertThat(response.mayWithdraw()).isTrue();

        assertThat(response.activeBetLimit()).isNull();

        assertThat(response.activeWithdrawalLimit()).isNotNull();
        assertThat(response.activeWithdrawalLimit().amount()).isEqualTo(500);
    }

    @Test
    void checkEligibility_shouldCombineMultipleBlockReasons() {
        Long playerId = 123L;
        ComplianceProfile profile = profile(playerId, false, true, ComplianceProfileRiskLevel.HIGH);

        GamblingLimit zeroBetLimit = activeLimit(profile, GamblingLimitType.BET, 0);
        GamblingLimit zeroWithdrawalLimit = activeLimit(profile, GamblingLimitType.WITHDRAWAL, 0);

        ComplianceFlag highFlag = unresolvedFlag(
            profile,
            ComplianceFlagType.MANUAL_RISK_LEVEL,
            ComplianceFlagSeverity.HIGH
        );

        mockEligibilityData(
            playerId,
            profile,
            List.of(zeroBetLimit, zeroWithdrawalLimit),
            List.of(highFlag)
        );

        EligibilityResponseDTO response = eligibilityService.checkEligibility(playerId);

        assertThat(response.mayBet()).isFalse();
        assertThat(response.mayWithdraw()).isFalse();
        assertThat(response.blockReasons()).containsExactlyInAnyOrder(
            AGE_NOT_VERIFIED,
            SELF_EXCLUDED,
            HIGH_RISK_PROFILE,
            AML_REVIEW_REQUIRED,
            BET_LIMIT_ZERO,
            WITHDRAWAL_LIMIT_ZERO
        );

        assertThat(response.activeBetLimit()).isNotNull();
        assertThat(response.activeBetLimit().amount()).isZero();

        assertThat(response.activeWithdrawalLimit()).isNotNull();
        assertThat(response.activeWithdrawalLimit().amount()).isZero();
    }

    @Test
    void checkEligibility_shouldThrow_whenComplianceProfileDoesNotExist() {
        Long playerId = 999L;

        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eligibilityService.checkEligibility(playerId))
            .isInstanceOf(ComplianceProfileMissingException.class);
    }

    private void mockEligibilityData(
        Long playerId,
        ComplianceProfile profile,
        List<GamblingLimit> activeLimits,
        List<ComplianceFlag> flags
    ) {
        when(complianceProfileRepository.findFirstByPlayerProfileId(playerId))
            .thenReturn(Optional.of(profile));

        when(gamblingLimitRepository.findActiveByComplianceId(
            eq(profile.getComplianceId()),
            any(OffsetDateTime.class)
        )).thenReturn(activeLimits);

        when(complianceFlagRepository.findByComplianceProfile_ComplianceId(profile.getComplianceId()))
            .thenReturn(flags);
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

    private ComplianceFlag unresolvedFlag(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity
    ) {
        ComplianceFlag flag = new ComplianceFlag();
        flag.setFlagId(20L);
        flag.setComplianceProfile(profile);
        flag.setType(type);
        flag.setSeverity(severity);
        flag.setCreatedDate(OffsetDateTime.now().minusDays(1));
        flag.setResolvedDate(null);
        return flag;
    }

    private ComplianceFlag resolvedFlag(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity
    ) {
        ComplianceFlag flag = new ComplianceFlag();
        flag.setFlagId(21L);
        flag.setComplianceProfile(profile);
        flag.setType(type);
        flag.setSeverity(severity);
        flag.setCreatedDate(OffsetDateTime.now().minusDays(2));
        flag.setResolvedDate(OffsetDateTime.now().minusDays(1));
        return flag;
    }
}