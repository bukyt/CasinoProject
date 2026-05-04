package com.casino.userservice.users.dto;

import lombok.Data;

@Data
public class PreferencesRequest {
    private String language;
    private String currency;
}
