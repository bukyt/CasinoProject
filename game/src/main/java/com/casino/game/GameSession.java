package com.casino.game;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class GameSession {
    private String id;
    private String gameId;
    private double balance;
    private String status; // "active", "closed"
    private List<Bet> bets;

    public GameSession(String id, String gameId, double initialBalance) {
        this.id = id;
        this.gameId = gameId;
        this.balance = initialBalance;
        this.status = "active";
        this.bets = new ArrayList<>();
    }
}