package com.casino.bonus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerBonus {

    private String playerId;
    private String bonusId;
    private double requiredWagering;
    private double progress;
    private String status;

    public boolean isActive() {
        return "active".equals(status);
    }
}