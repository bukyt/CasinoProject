package com.casino.game;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GameSession {

    @Id
    private String id;

    private String gameId;

    private Integer playerProfileId;

    private double balance;

    private String status;

    private Boolean hasActiveBonus = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("gameSession")
    private List<Bet> bets = new ArrayList<>();
}