package com.casino.bonus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/bonuses")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class BonusController {

    private final Map<String, Bonus> bonuses = new HashMap<>();
    private final Map<String, List<PlayerBonus>> playerBonuses = new HashMap<>();
    private final AtomicLong bonusIdCounter = new AtomicLong(1);
    private final BonusEventConsumer consumer;

    public BonusController(BonusEventConsumer consumer) {
        this.consumer = consumer;
    }

    // CREATE BONUS
    @PostMapping
    public ResponseEntity<Bonus> createBonus(@RequestBody CreateBonusRequest request) {
        String id = "bonus-" + bonusIdCounter.getAndIncrement();
        Bonus bonus = new Bonus(id, request.getName(), request.getDescription(), request.getWageringRequirement());
        bonuses.put(id, bonus);
        return ResponseEntity.ok(bonus);
    }

    // LIST ALL DEFINED BONUSES
    @GetMapping
    public ResponseEntity<List<Bonus>> listBonuses() {
        return ResponseEntity.ok(new ArrayList<>(bonuses.values()));
    }

    // GET SPECIFIC BONUS DEFINITION
    @GetMapping("/{id}")
    public ResponseEntity<Bonus> getBonus(@PathVariable String id) {
        Bonus bonus = bonuses.get(id);
        if (bonus == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bonus);
    }

    // ASSIGN A BONUS TO A PLAYER
    @PostMapping("/{id}/assign")
    public ResponseEntity<PlayerBonus> assignBonus(
            @PathVariable String id,
            @RequestBody AssignBonusRequest request
    ) {
        Bonus bonus = bonuses.get(id);
        if (bonus == null) return ResponseEntity.notFound().build();

        PlayerBonus pb = new PlayerBonus(
                request.getPlayerId(),
                id,
                bonus.getWageringRequirement(),
                0,
                "active"
        );

        playerBonuses.computeIfAbsent(request.getPlayerId(), k -> new ArrayList<>()).add(pb);
        return ResponseEntity.ok(pb);
    }

    // LIST BONUSES ASSIGNED TO A SPECIFIC PLAYER
    @GetMapping("/players/{playerId}")
    public ResponseEntity<List<PlayerBonus>> listPlayerBonuses(@PathVariable String playerId) {
        return ResponseEntity.ok(playerBonuses.getOrDefault(playerId, new ArrayList<>()));
    }

    // GET PLAYER CREDIT BALANCE (Wrapped in Map for proper JSON content-type)
    @GetMapping("/players/{playerId}/credits")
    public ResponseEntity<Map<String, Double>> getPlayerCredits(@PathVariable String playerId) {
        Map<String, Double> response = new HashMap<>();
        response.put("balance", consumer.getPlayerCredits(playerId));
        return ResponseEntity.ok(response);
    }

    // DEBUG ADD CREDITS
    @PostMapping("/players/{playerId}/debug-add")
    public ResponseEntity<Map<String, Double>> debugAddCredits(
            @PathVariable String playerId, 
            @RequestBody Map<String, Double> payload) {
        
        Double amount = payload.getOrDefault("amount", 10.0);
        consumer.addDebugCredits(playerId, amount);
        
        Map<String, Double> response = new HashMap<>();
        response.put("balance", consumer.getPlayerCredits(playerId));
        return ResponseEntity.ok(response);
    }

    // ---- DTOs ----
    public static class CreateBonusRequest {
        private String name;
        private String description;
        private double wageringRequirement;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getWageringRequirement() { return wageringRequirement; }
        public void setWageringRequirement(double wageringRequirement) { this.wageringRequirement = wageringRequirement; }
    }

    public static class AssignBonusRequest {
        private String playerId;
        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
    }
}