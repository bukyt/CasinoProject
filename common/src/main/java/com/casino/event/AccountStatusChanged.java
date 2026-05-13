package com.casino.event;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusChanged {

    @NotBlank(message = "accountId should be specified")
    private String accountId;

    @NotBlank(message = "status should be specified")
    private String status;

    @NotNull(message = "changedAt should be specified")
    private Instant changedAt;
}
