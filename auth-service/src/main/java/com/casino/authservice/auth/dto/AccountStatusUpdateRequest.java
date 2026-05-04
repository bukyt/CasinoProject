package com.casino.authservice.auth.dto;

import com.casino.authservice.auth.model.AccountStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountStatusUpdateRequest {

    @NotNull
    private AccountStatus status;
}
