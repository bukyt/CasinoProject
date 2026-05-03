package com.casino.dto;

import com.casino.model.ComplianceProfileRiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyComplianceProfileDTO {

    private Boolean ageVerified;
    private Boolean selfExcluded;
    private ComplianceProfileRiskLevel riskLevel;

}
