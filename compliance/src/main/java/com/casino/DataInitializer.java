package com.casino;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadComplianceData(
        ComplianceProfileRepository complianceProfileRepository,
        ComplianceFlagRepository complianceFlagRepository,
        GamblingLimitRepository gamblingLimitRepository
    ) {
        return args -> {
            if (complianceProfileRepository.count() > 0) {
                return;
            }

            OffsetDateTime now = OffsetDateTime.now();

            /*
             * Player 1001:
             * Clean player, eligible to bet and withdraw.
             * Has a resolved historical age-verification flag.
             */
            ComplianceProfile cleanProfile = new ComplianceProfile();
            cleanProfile.setPlayerProfileId(1001L);
            cleanProfile.setAgeVerified(true);
            cleanProfile.setSelfExcluded(false);
            cleanProfile.setRiskLevel(ComplianceProfileRiskLevel.LOW);
            cleanProfile.setLastReviewDate(now.minusDays(1));

            cleanProfile = complianceProfileRepository.save(cleanProfile);

            complianceFlagRepository.save(createFlag(
                cleanProfile,
                ComplianceFlagType.NO_AGE_VERIFICATION,
                ComplianceFlagSeverity.RESOLVED_ADMIN,
                now.minusDays(10),
                now.minusDays(8)
            ));

            gamblingLimitRepository.save(createLimit(
                cleanProfile,
                GamblingLimitType.BET,
                100,
                GamblingLimitPeriod.DAILY,
                now.minusDays(1),
                null,
                null
            ));

            gamblingLimitRepository.save(createLimit(
                cleanProfile,
                GamblingLimitType.WITHDRAWAL,
                500,
                GamblingLimitPeriod.WEEKLY,
                now.minusDays(1),
                null,
                null
            ));


            /*
             * Player 1002:
             * Age not verified, should fail eligibility because of profile state
             * and unresolved NO_AGE_VERIFICATION flag.
             */
            ComplianceProfile ageNotVerifiedProfile = new ComplianceProfile();
            ageNotVerifiedProfile.setPlayerProfileId(1002L);
            ageNotVerifiedProfile.setAgeVerified(false);
            ageNotVerifiedProfile.setSelfExcluded(false);
            ageNotVerifiedProfile.setRiskLevel(ComplianceProfileRiskLevel.UNASSESSED);
            ageNotVerifiedProfile.setLastReviewDate(now.minusHours(12));

            ageNotVerifiedProfile = complianceProfileRepository.save(ageNotVerifiedProfile);

            complianceFlagRepository.save(createFlag(
                ageNotVerifiedProfile,
                ComplianceFlagType.NO_AGE_VERIFICATION,
                ComplianceFlagSeverity.MEDIUM,
                now.minusHours(12),
                null
            ));

            gamblingLimitRepository.save(createLimit(
                ageNotVerifiedProfile,
                GamblingLimitType.BET,
                50,
                GamblingLimitPeriod.DAILY,
                now.minusHours(12),
                null,
                null
            ));


            /*
             * Player 1003:
             * Self-excluded player. Betting should be blocked.
             */
            ComplianceProfile selfExcludedProfile = new ComplianceProfile();
            selfExcludedProfile.setPlayerProfileId(1003L);
            selfExcludedProfile.setAgeVerified(true);
            selfExcludedProfile.setSelfExcluded(true);
            selfExcludedProfile.setRiskLevel(ComplianceProfileRiskLevel.LOW);
            selfExcludedProfile.setLastReviewDate(now.minusHours(6));

            selfExcludedProfile = complianceProfileRepository.save(selfExcludedProfile);

            complianceFlagRepository.save(createFlag(
                selfExcludedProfile,
                ComplianceFlagType.SELF_EXCLUSION,
                ComplianceFlagSeverity.LOW,
                now.minusHours(6),
                null
            ));

            gamblingLimitRepository.save(createLimit(
                selfExcludedProfile,
                GamblingLimitType.BET,
                0,
                GamblingLimitPeriod.DAILY,
                now.minusHours(6),
                null,
                null
            ));

            gamblingLimitRepository.save(createLimit(
                selfExcludedProfile,
                GamblingLimitType.WITHDRAWAL,
                250,
                GamblingLimitPeriod.WEEKLY,
                now.minusHours(6),
                null,
                null
            ));

            complianceFlagRepository.save(createFlag(
                selfExcludedProfile,
                ComplianceFlagType.NO_AGE_VERIFICATION,
                ComplianceFlagSeverity.RESOLVED_ADMIN,
                now.minusDays(10),
                now.minusDays(8)
            ));

            /*
             * Player 1004:
             * Manually high-risk profile.
             */
            ComplianceProfile manualRiskProfile = new ComplianceProfile();
            manualRiskProfile.setPlayerProfileId(1004L);
            manualRiskProfile.setAgeVerified(true);
            manualRiskProfile.setSelfExcluded(false);
            manualRiskProfile.setRiskLevel(ComplianceProfileRiskLevel.HIGH);
            manualRiskProfile.setLastReviewDate(now.minusDays(2));

            manualRiskProfile = complianceProfileRepository.save(manualRiskProfile);

            complianceFlagRepository.save(createFlag(
                manualRiskProfile,
                ComplianceFlagType.NO_AGE_VERIFICATION,
                ComplianceFlagSeverity.RESOLVED_ADMIN,
                now.minusDays(10),
                now.minusDays(8)
            ));

            complianceFlagRepository.save(createFlag(
                manualRiskProfile,
                ComplianceFlagType.MANUAL_RISK_LEVEL,
                ComplianceFlagSeverity.HIGH,
                now.minusDays(2),
                null
            ));

            gamblingLimitRepository.save(createLimit(
                manualRiskProfile,
                GamblingLimitType.BET,
                25,
                GamblingLimitPeriod.DAILY,
                now.minusDays(2),
                null,
                null
            ));

            gamblingLimitRepository.save(createLimit(
                manualRiskProfile,
                GamblingLimitType.WITHDRAWAL,
                0,
                GamblingLimitPeriod.WEEKLY,
                now.minusDays(2),
                null,
                null
            ));


            /*
             * Player 1005:
             * Has resolved historical flags and a revoked old limit.
             * Current state should be mostly clean.
             */
            ComplianceProfile resolvedHistoryProfile = new ComplianceProfile();
            resolvedHistoryProfile.setPlayerProfileId(1005L);
            resolvedHistoryProfile.setAgeVerified(true);
            resolvedHistoryProfile.setSelfExcluded(false);
            resolvedHistoryProfile.setRiskLevel(ComplianceProfileRiskLevel.LOW);
            resolvedHistoryProfile.setLastReviewDate(now.minusHours(2));

            resolvedHistoryProfile = complianceProfileRepository.save(resolvedHistoryProfile);

            complianceFlagRepository.save(createFlag(
                resolvedHistoryProfile,
                ComplianceFlagType.SELF_EXCLUSION,
                ComplianceFlagSeverity.RESOLVED_ADMIN,
                now.minusDays(10),
                now.minusDays(3)
            ));

            complianceFlagRepository.save(createFlag(
                resolvedHistoryProfile,
                ComplianceFlagType.MANUAL_RISK_LEVEL,
                ComplianceFlagSeverity.RESOLVED,
                now.minusDays(8),
                now.minusDays(4)
            ));

            gamblingLimitRepository.save(createLimit(
                resolvedHistoryProfile,
                GamblingLimitType.BET,
                10,
                GamblingLimitPeriod.DAILY,
                now.minusDays(10),
                null,
                now.minusDays(5)
            ));

            gamblingLimitRepository.save(createLimit(
                resolvedHistoryProfile,
                GamblingLimitType.BET,
                150,
                GamblingLimitPeriod.DAILY,
                now.minusDays(1),
                null,
                null
            ));

            gamblingLimitRepository.save(createLimit(
                resolvedHistoryProfile,
                GamblingLimitType.WITHDRAWAL,
                1000,
                GamblingLimitPeriod.MONTHLY,
                now.minusDays(1),
                null,
                null
            ));
        };
    }

    private ComplianceFlag createFlag(
        ComplianceProfile profile,
        ComplianceFlagType type,
        ComplianceFlagSeverity severity,
        OffsetDateTime createdDate,
        OffsetDateTime resolvedDate
    ) {
        ComplianceFlag flag = new ComplianceFlag();
        flag.setComplianceProfile(profile);
        flag.setType(type);
        flag.setSeverity(severity);
        flag.setCreatedDate(createdDate);
        flag.setResolvedDate(resolvedDate);
        return flag;
    }

    private GamblingLimit createLimit(
        ComplianceProfile profile,
        GamblingLimitType type,
        int amount,
        GamblingLimitPeriod period,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        OffsetDateTime revokedDate
    ) {
        GamblingLimit limit = new GamblingLimit();
        limit.setComplianceProfile(profile);
        limit.setType(type);
        limit.setAmount(amount);
        limit.setPeriod(period);
        limit.setCreatedDate(startDate);
        limit.setStartDate(startDate);
        limit.setEndDate(endDate);
        limit.setRevokedDate(revokedDate);
        return limit;
    }
}