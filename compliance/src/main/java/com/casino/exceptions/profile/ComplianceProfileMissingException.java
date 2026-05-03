package com.casino.exceptions.profile;


import com.casino.exceptions.NotFoundException;

import java.util.Map;

import static com.casino.exceptions.CommonApiErrorCodes.COMPLIANCE_PROFILE_MISSING;

public class ComplianceProfileMissingException extends NotFoundException {

    public static final String EXAMPLE = """
        {
          "code": "compliance.profile.missing",
          "message": "Compliance profile for player 123 is missing",
          "data": {
            "playerProfileId": "123"
          }
        }
        """;

    public ComplianceProfileMissingException(Long playerId) {
        super(COMPLIANCE_PROFILE_MISSING, "Compliance profile for player " + playerId + " is missing",
                Map.of("playerId", playerId.toString()));
    }
}
