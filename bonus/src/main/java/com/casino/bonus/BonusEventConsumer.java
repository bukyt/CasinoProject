package com.casino.bonus;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BonusEventConsumer {

    // Tracks total wagered by player
    private final Map<Integer, Double> wageringTracker = new HashMap<>();

    // Tracks bonus credits awarded
    private final Map<Integer, Double> bonusCredits = new HashMap<>();

    @KafkaListener(
            topics = "betplaced",
            groupId = "bonus-group"
    )
    public void consume(BetPlaced event) {

        Integer playerId = event.getPlayerProfileId();

        double amount = event.getAmount().doubleValue();

        double currentWagered =
                wageringTracker.getOrDefault(playerId, 0.0);

        currentWagered += amount;

        wageringTracker.put(playerId, currentWagered);

        // Every 50 wagered => 10 credits
        int rewardBlocks = (int) (currentWagered / 50);

        double expectedCredits = rewardBlocks * 10;

        double currentCredits =
                bonusCredits.getOrDefault(playerId, 0.0);

        if (expectedCredits > currentCredits) {

            double newlyAwarded =
                    expectedCredits - currentCredits;

            bonusCredits.put(playerId, expectedCredits);

            System.out.println(
                    "BONUS AWARDED -> Player "
                    + playerId
                    + " received "
                    + newlyAwarded
                    + " bonus credits"
            );
        }
    }

    public Double getPlayerCredits(Integer playerId) {
        return bonusCredits.getOrDefault(playerId, 0.0);
    }
}