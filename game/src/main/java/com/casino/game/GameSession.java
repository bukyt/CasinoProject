package com.casino.game;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class GameSession {

    @Id
    private String id;

    private String gameId;

    private Integer playerProfileId;

    private double balance;

    private String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bet> bets = new ArrayList<>();
}