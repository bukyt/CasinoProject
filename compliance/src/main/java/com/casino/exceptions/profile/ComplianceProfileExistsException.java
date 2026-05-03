package com.casino.exceptions.profile;


import com.casino.exceptions.ConflictException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_PROFILE_EXISTS;

public class ComplianceProfileExistsException extends ConflictException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.profile.exists",
          "message": "Compliance profile for player 123 already exists",
          "data": {
            "playerProfileId": "123"
          }
        }
        """;

    public ComplianceProfileExistsException(Long playerId) {
        super(COMPLIANCE_PROFILE_EXISTS, "Compliance profile for player " + playerId + " already exists",
                Map.of("playerId", playerId.toString()));
    }
}
