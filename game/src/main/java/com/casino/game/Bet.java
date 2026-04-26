package com.casino.game;

import lombok.Data;

@Data
public class Bet {
    private double amount;
    private String outcome; // "win", "loss"
    private double payout;

    public Bet(double amount, String outcome, double payout) {
        this.amount = amount;
        this.outcome = outcome;
        this.payout = payout;
    }
}