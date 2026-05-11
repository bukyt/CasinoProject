package com.casino.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @Column(name = "player_profile_id", nullable = false, updatable = false)
    private Integer playerProfileId;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    protected Wallet() {
    }

    public Wallet(Integer playerProfileId, BigDecimal availableBalance) {
        this.playerProfileId = playerProfileId;
        this.availableBalance = availableBalance;
    }

    public Integer getPlayerProfileId() {
        return playerProfileId;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
}
