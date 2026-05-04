package com.casino.userservice.users.dto;

import java.time.LocalDate;

import com.casino.userservice.users.model.ProfileStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerProfileResponse {
    private String playerProfileId;
    private String accountId;
    private String fullName;
    private LocalDate dateOfBirth;
    private ProfileStatus status;
    private String email;
    private String phone;
    private String address;
    private String language;
    private String currency;
}
