package com.casino.bonus;

import java.math.BigDecimal;

public class BetPlaced {
    // CHANGE: Integer to String
    private Integer playerProfileId; 
    private BigDecimal amount;

    public Integer getPlayerProfileId() {
        return playerProfileId;
    }

    public void setPlayerProfileId(Integer playerProfileId) {
        this.playerProfileId = playerProfileId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}