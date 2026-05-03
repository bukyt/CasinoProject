package com.casino.exceptions.limit;

import java.util.Map;

import com.casino.exceptions.ConflictException;
import com.casino.model.GamblingLimitPeriod;
import com.casino.model.GamblingLimitType;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_LIMIT_EXISTS;

public class ComplianceLimitExistsException extends ConflictException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.limit.exists",
          "message": "Active DEPOSIT DAILY gambling limit for player 123 already exists",
          "data": {
            "playerId": "123",
            "type": "DEPOSIT",
            "period": "DAILY"
          }
        }
        """;

    public ComplianceLimitExistsException(
            Long playerId,
            GamblingLimitType type,
            GamblingLimitPeriod period
    ) {
        super(
                COMPLIANCE_LIMIT_EXISTS,
                "Active " + type + " " + period + " gambling limit for player " + playerId + " already exists",
                Map.of(
                        "playerId", playerId.toString(),
                        "type", type.name(),
                        "period", period.name()
                )
        );
    }
}