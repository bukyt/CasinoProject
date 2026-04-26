package com.casino.game;

import lombok.Data;

@Data
public class Game {
    private String id;
    private String name;
    private String description;

    public Game(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}