package com.casino.authservice.auth.dto;

import java.time.Instant;
import java.util.List;

import com.casino.authservice.auth.model.AccountStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String accountId;
    private String username;
    private AccountStatus status;
    private Instant createdDate;
    private List<String> roles;
}
