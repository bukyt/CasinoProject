package com.casino.bonus;

import lombok.Data;

@Data
public class PlayerBonus {
    private String playerId;
    private String bonusId;
    private double wageringRequired;
    private double wageringProgress;
    private String status; // active, completed

    public PlayerBonus(String playerId, String bonusId,
                       double wageringRequired,
                       double wageringProgress,
                       String status) {
        this.playerId = playerId;
        this.bonusId = bonusId;
        this.wageringRequired = wageringRequired;
        this.wageringProgress = wageringProgress;
        this.status = status;
    }
}