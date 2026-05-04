package com.casino.bonus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/bonuses")
public class BonusController {

    private final Map<String, Bonus> bonuses = new HashMap<>();
    private final Map<String, List<PlayerBonus>> playerBonuses = new HashMap<>();
    private final AtomicLong bonusIdCounter = new AtomicLong(1);

    // CREATE BONUS
    @PostMapping
    public ResponseEntity<Bonus> createBonus(@RequestBody CreateBonusRequest request) {
        String id = "bonus-" + bonusIdCounter.getAndIncrement();
        Bonus bonus = new Bonus(id, request.getName(), request.getDescription(), request.getWageringRequirement());

        bonuses.put(id, bonus);
        return ResponseEntity.ok(bonus);
    }

    // LIST BONUSES
    @GetMapping
    public ResponseEntity<List<Bonus>> listBonuses() {
        return ResponseEntity.ok(new ArrayList<>(bonuses.values()));
    }

    // GET BONUS BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Bonus> getBonus(@PathVariable String id) {
        Bonus bonus = bonuses.get(id);
        if (bonus == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(bonus);
    }

    // ASSIGN BONUS TO PLAYER
    @PostMapping("/{id}/assign")
    public ResponseEntity<PlayerBonus> assignBonus(
            @PathVariable String id,
            @RequestBody AssignBonusRequest request
    ) {
        Bonus bonus = bonuses.get(id);
        if (bonus == null) {
            return ResponseEntity.notFound().build();
        }

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

    // LIST PLAYER BONUSES
    @GetMapping("/players/{playerId}")
    public ResponseEntity<List<PlayerBonus>> listPlayerBonuses(@PathVariable String playerId) {
        return ResponseEntity.ok(playerBonuses.getOrDefault(playerId, new ArrayList<>()));
    }

    // ---- REQUEST DTOs ----

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