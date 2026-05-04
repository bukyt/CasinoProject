package com.casino.authservice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 120)
    private String username;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;
}
