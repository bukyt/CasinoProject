package com.casino.dto.eligibility;

public enum EligibilityBlockReason {
    AGE_NOT_VERIFIED,
    SELF_EXCLUDED,
    HIGH_RISK_PROFILE,
    CRITICAL_RISK_PROFILE,
    AML_REVIEW_REQUIRED,
    BET_LIMIT_ZERO,
    WITHDRAWAL_LIMIT_ZERO
}