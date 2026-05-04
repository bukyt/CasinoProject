package com.casino.exceptions.flag;

import com.casino.exceptions.ConflictException;
import com.casino.model.flag.ComplianceFlagType;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_FLAG_EXISTS;

public class ComplianceFlagExistsException extends ConflictException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.flag.exists",
          "message": "Active AML_REVIEW HIGH compliance flag for player 123 already exists",
          "data": {
            "playerId": "123",
            "type": "AML_REVIEW",
            "severity": "HIGH"
          }
        }
        """;

    public ComplianceFlagExistsException(
        Long playerId,
        ComplianceFlagType type
    ) {
        super(
            COMPLIANCE_FLAG_EXISTS,
            "Active " + type + " compliance flag for player " + playerId + " already exists",
            Map.of(
                "playerId", playerId.toString(),
                "type", type.name()
            )
        );
    }
}