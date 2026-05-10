package com.casino.bonus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/bonuses")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH}
)
public class BonusController {

    private final Map<String, Bonus> bonuses = new HashMap<>();
    private final Map<String, List<PlayerBonus>> playerBonuses = new HashMap<>();
    private final AtomicLong bonusIdCounter = new AtomicLong(1);
    private final BonusEventConsumer consumer;

    public BonusController(BonusEventConsumer consumer) {
        this.consumer = consumer;
    }

    // ---------------- CREATE BONUS ----------------
    @PostMapping
    public ResponseEntity<Bonus> createBonus(@RequestBody CreateBonusRequest request) {
        String id = "bonus-" + bonusIdCounter.getAndIncrement();

        Bonus bonus = new Bonus(
                id,
                request.getName(),
                request.getDescription(),
                request.getWageringRequirement()
        );

        bonuses.put(id, bonus);
        return ResponseEntity.ok(bonus);
    }

    // ---------------- LIST BONUSES ----------------
    @GetMapping
    public ResponseEntity<List<Bonus>> listBonuses() {
        return ResponseEntity.ok(new ArrayList<>(bonuses.values()));
    }

    // ---------------- GET BONUS ----------------
    @GetMapping("/{id}")
    public ResponseEntity<Bonus> getBonus(@PathVariable String id) {
        Bonus bonus = bonuses.get(id);
        if (bonus == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bonus);
    }

    // ---------------- ASSIGN BONUS ----------------
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

        playerBonuses
                .computeIfAbsent(request.getPlayerId(), k -> new ArrayList<>())
                .add(pb);

        return ResponseEntity.ok(pb);
    }

    // ---------------- PLAYER BONUSES ----------------
    @GetMapping("/players/{playerId}")
    public ResponseEntity<List<PlayerBonus>> listPlayerBonuses(@PathVariable String playerId) {

        return ResponseEntity.ok(
                playerBonuses.computeIfAbsent(playerId, k -> new ArrayList<>())
        );
    }

    // ---------------- HAS ACTIVE BONUS ----------------
    @GetMapping("/players/{playerId}/has-active-bonus")
    public ResponseEntity<Boolean> hasActiveBonus(@PathVariable String playerId) {

        List<PlayerBonus> bonuses =
            playerBonuses.computeIfAbsent(playerId, k -> new ArrayList<>());

        boolean active = bonuses.stream()
                .anyMatch(b -> "active".equals(b.getStatus()));

        return ResponseEntity.ok(active);
    }

    // ---------------- CONSUME BONUS (FIXED) ----------------
    @PatchMapping("/players/{playerId}/consume")
    public ResponseEntity<?> consumeBonus(@PathVariable String playerId) {

        List<PlayerBonus> list = playerBonuses.get(playerId);

        if (list == null || list.isEmpty()) {
            return ResponseEntity.status(404).body("No bonuses found");
        }

        for (PlayerBonus bonus : list) {
            if ("active".equals(bonus.getStatus())) {
                bonus.setStatus("used");
                return ResponseEntity.ok(bonus);
            }
        }

        return ResponseEntity.status(404).body("No active bonus");
    }

    // ---------------- GRANT FREE SPIN (FIXED NO DUPLICATES) ----------------
    @PostMapping("/players/{playerId}/grant-free-spin")
    public ResponseEntity<PlayerBonus> grantFreeSpin(@PathVariable String playerId) {

        List<PlayerBonus> list =
                playerBonuses.computeIfAbsent(playerId, k -> new ArrayList<>());

        // prevent duplicate active free spins
        boolean alreadyActive = list.stream()
                .anyMatch(b -> "active".equals(b.getStatus())
                        && "free-spin".equals(b.getBonusId()));

        if (alreadyActive) {
            return ResponseEntity.status(409).build();
        }

        PlayerBonus pb = new PlayerBonus(
                playerId,
                "free-spin",
                0,
                0,
                "active"
        );

        list.add(pb);

        return ResponseEntity.ok(pb);
    }

    // ---------------- DTOs ----------------
    public static class CreateBonusRequest {
        private String name;
        private String description;
        private double wageringRequirement;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getWageringRequirement() { return wageringRequirement; }
        public void setWageringRequirement(double wageringRequirement) {
            this.wageringRequirement = wageringRequirement;
        }
    }

    public static class AssignBonusRequest {
        private String playerId;

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
    }
}