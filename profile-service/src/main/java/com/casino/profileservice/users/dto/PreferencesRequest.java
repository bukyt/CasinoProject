package com.casino.profileservice.users.dto;

import lombok.Data;

@Data
public class PreferencesRequest {
    private String language;
    private String currency;
}
