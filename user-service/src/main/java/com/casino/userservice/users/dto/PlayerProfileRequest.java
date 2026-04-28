package com.casino.userservice.users.dto;

import java.time.LocalDate;

import com.casino.userservice.users.model.ProfileStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlayerProfileRequest {
    @NotBlank
    private String accountId;

    @NotBlank
    private String fullName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private ProfileStatus status;

    private String email;
    private String phone;
    private String address;
    private String language;
    private String currency;
}
