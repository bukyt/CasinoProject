package com.casino.bonus;

import lombok.Data;

@Data
public class Bonus {
    private String id;
    private String name;
    private String description;
    private double wageringRequirement;

    public Bonus(String id, String name, String description, double wageringRequirement) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.wageringRequirement = wageringRequirement;
    }
}