package com.casino.userservice.users.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ContactDetailsRequest {
    @Email
    private String email;
    private String phone;
    private String address;
}
