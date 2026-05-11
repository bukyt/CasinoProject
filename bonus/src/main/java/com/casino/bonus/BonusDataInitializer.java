package com.casino.bonus;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BonusDataInitializer implements CommandLineRunner {

    private final BonusController bonusController;

    public BonusDataInitializer(BonusController bonusController) {
        this.bonusController = bonusController;
    }

    @Override
    public void run(String... args) {

        BonusController.CreateBonusRequest welcome = new BonusController.CreateBonusRequest();
        welcome.name = "Welcome Bonus";
        welcome.description = "Free starting bonus";
        welcome.wageringRequirement = 10.0;

        bonusController.createBonus(welcome);

        BonusController.CreateBonusRequest freeSpin = new BonusController.CreateBonusRequest();
        freeSpin.name = "Free Spin Bonus";
        freeSpin.description = "Random free spin reward";
        freeSpin.wageringRequirement = 0.0;

        bonusController.createBonus(freeSpin);

        System.out.println("BONUS SYSTEM INITIALIZED");
    }
}