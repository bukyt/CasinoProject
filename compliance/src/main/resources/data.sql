INSERT INTO compliance_profile (
    compliance_id,
    player_profile_id,
    is_age_verified,
    is_self_excluded,
    risk_level,
    last_review_date
)
VALUES (
    1,
    1001,
    TRUE,
    FALSE,
    'LOW',
    CURRENT_TIMESTAMP
);

INSERT INTO gambling_limits (
    limit_id,
    compliance_id,
    limit_type,
    amount,
    period,
    created_date,
    start_date,
    end_date,
    revoked_date
)
VALUES (
    1,
    1,
    'BET',
    100,
    'DAILY',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    NULL
);

INSERT INTO gambling_limits (
    limit_id,
    compliance_id,
    limit_type,
    amount,
    period,
    created_date,
    start_date,
    end_date,
    revoked_date
)
VALUES (
    2,
    1,
    'DEPOSIT',
    500,
    'WEEKLY',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    NULL
);

INSERT INTO compliance_flags (
    flag_id,
    compliance_id,
    flag_type,
    severity,
    created_date,
    resolved_date
)
VALUES (
    1,
    1,
    'NO_AGE_VERIFICATION',
    'LOW',
    CURRENT_TIMESTAMP,
    NULL
);