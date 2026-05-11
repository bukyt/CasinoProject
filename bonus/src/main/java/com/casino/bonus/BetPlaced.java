package com.casino.bonus;

import java.math.BigDecimal;

public class BetPlaced {
    // CHANGE: Integer to String
    private String playerProfileId; 
    private BigDecimal amount;

    public String getPlayerProfileId() {
        return playerProfileId;
    }

    public void setPlayerProfileId(String playerProfileId) {
        this.playerProfileId = playerProfileId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}