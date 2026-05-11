package com.casino.bonus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Component
public class BonusEventConsumer {

    private final ObjectMapper objectMapper;

    private final Map<String, Double> wageringTracker = new ConcurrentHashMap<>();
    private final Map<String, Double> bonusWallet = new ConcurrentHashMap<>();
    private final CountDownLatch testLatch = new CountDownLatch(1);

    public void awaitProcessing() throws InterruptedException {
        testLatch.await();
    }

    public BonusEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "betplaced", groupId = "bonus-group")
    public void consume(ConsumerRecord<String, Object> record) {
        Object value = record.value();
        Map<String, Object> event;

        try {
            if (value instanceof Map<?, ?> map) {
                event = (Map<String, Object>) map;
            } else {
                event = objectMapper.convertValue(value, Map.class);
            }
        } catch (Exception e) {
            System.err.println("FAILED TO PARSE EVENT: " + value);
            return;
        }

        process(event);
    }

    public void process(Map<String, Object> event) {
        if (event == null) return;

        String playerId = String.valueOf(event.get("playerProfileId"));
        double amount = Double.parseDouble(event.get("amount").toString());

        double current = wageringTracker.getOrDefault(playerId, 0.0) + amount;
        wageringTracker.put(playerId, current);

        int blocks = (int) (current / 50);
        double expectedBonus = blocks * 10.0;

        double currentBonus = bonusWallet.getOrDefault(playerId, 0.0);

        if (expectedBonus > currentBonus) {
            bonusWallet.put(playerId, expectedBonus);

            System.out.println("BONUS UPDATED -> " + playerId + " = " + expectedBonus);
        }
    }

    public boolean consumeBonus(String playerId, double amount) {
        double available = bonusWallet.getOrDefault(playerId, 0.0);

        if (available < amount) return false;

        bonusWallet.put(playerId, available - amount);
        return true;
    }

    public void addDebugCredits(Integer playerId, double amount) {
        bonusWallet.merge(playerId.toString(), amount, Double::sum);
    }

    public Double getPlayerCredits(Integer playerId) {
        return bonusWallet.getOrDefault(playerId, 0.0);
    }
    
    public boolean isFreeSpinActive(Integer playerId) {
        return false; // REST is source of truth
    }
}