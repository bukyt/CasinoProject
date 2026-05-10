package com.casino.bonus;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BonusEventConsumer {

    private final Map<String, Double> wageringTracker = new HashMap<>();
    private final Map<String, Double> bonusCredits = new HashMap<>();

    @KafkaListener(topics = "betplaced", groupId = "bonus-group")
    public void consume(BetPlaced event) {
        // --- SAFETY CHECK (Prevents NullPointerException) ---
        if (event == null || event.getAmount() == null || event.getPlayerProfileId() == null) {
            System.err.println("RECEIVED NULL DATA: Skipping malformed bet event.");
            System.err.println("Event data: " + event);
            return; 
        }
        // ----------------------------------------------------

        String playerId = event.getPlayerProfileId();
        double amount = event.getAmount().doubleValue();
        
        double currentWagered = wageringTracker.getOrDefault(playerId, 0.0);
        currentWagered += amount;
        wageringTracker.put(playerId, currentWagered);

        int rewardBlocks = (int) (currentWagered / 50);
        double expectedCredits = rewardBlocks * 10;
        
        // We only award if the calculation gives them more than they currently have
        // (Note: This logic only tracks milestones, it doesn't handle the session win/loss)
        double currentCredits = bonusCredits.getOrDefault(playerId, 0.0);

        if (expectedCredits > currentCredits) {
            bonusCredits.put(playerId, expectedCredits);
            System.out.println("BONUS AWARDED -> Player " + playerId + " received credits");
        }
    }

    public void addDebugCredits(String playerId, Double amount) {
        bonusCredits.merge(playerId, amount, Double::sum);
    }

    public Double getPlayerCredits(String playerId) {
        return bonusCredits.getOrDefault(playerId, 0.0);
    }

    @KafkaListener(topics = "session-closed", groupId = "bonus-group")
    public void consumeSessionClosed(Map<String, Object> event) {
        // Safety check for session closure too
        if (event.get("playerProfileId") == null || event.get("balance") == null) {
            return;
        }

        String playerId = (String) event.get("playerProfileId");
        Double finalBalance = Double.valueOf(event.get("balance").toString());

        // This effectively "saves" the game session outcome back to the permanent balance
        bonusCredits.put(playerId, finalBalance);

        System.out.println("SESSION SYNC -> Player " + playerId + " new balance: " + finalBalance);
    }
}