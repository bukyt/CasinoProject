package com.casino.exceptions.limit;

import com.casino.exceptions.BadRequestException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.INVALID_COMPLIANCE_LIMIT;

public class InvalidComplianceLimitException extends BadRequestException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.limit.invalid",
          "message": "Limit end date must be after start date",
          "data": {
            "reason": "Limit end date must be after start date"
          }
        }
        """;

    public InvalidComplianceLimitException(String reason) {
        super(
                INVALID_COMPLIANCE_LIMIT,
                reason,
                Map.of("reason", reason)
        );
    }
}