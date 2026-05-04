package com.casino.game;

import lombok.Data;

@Data
public class CreateSessionRequest {
    private String gameId;
    private double initialBalance;
    private Integer playerProfileId;
}