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

    // ---------------- CREATE BONUS ----------------
    @PostMapping
    public ResponseEntity<Bonus> createBonus(@RequestBody CreateBonusRequest request) {

        String id = "bonus-" + bonusIdCounter.getAndIncrement();

        Bonus bonus = new Bonus(
                id,
                request.name,
                request.description,
                request.wageringRequirement
        );

        bonuses.put(id, bonus);
        return ResponseEntity.ok(bonus);
    }

    // ---------------- LIST ----------------
    @GetMapping
    public ResponseEntity<List<Bonus>> listBonuses() {
        return ResponseEntity.ok(new ArrayList<>(bonuses.values()));
    }

    // ---------------- ACTIVE CHECK ----------------
    @GetMapping("/players/{playerId}/has-active-bonus")
    public ResponseEntity<Boolean> hasActiveBonus(@PathVariable String playerId) {

        List<PlayerBonus> list =
                playerBonuses.computeIfAbsent(playerId, k -> new ArrayList<>());

        boolean active = list.stream()
                .anyMatch(PlayerBonus::isActive);

        return ResponseEntity.ok(active);
    }

    // ---------------- GRANT FREE SPIN ----------------
    @PostMapping("/players/{playerId}/grant-free-spin")
    public ResponseEntity<PlayerBonus> grantFreeSpin(@PathVariable String playerId) {

        List<PlayerBonus> list =
                playerBonuses.computeIfAbsent(playerId, k -> new ArrayList<>());

        boolean exists = list.stream()
                .anyMatch(b -> "free-spin".equals(b.getBonusId()) && b.isActive());

        if (exists) {
            return ResponseEntity.status(409).build();
        }

        PlayerBonus pb = new PlayerBonus(
                playerId,
                "free-spin",
                0,
                1,
                "active"
        );

        list.add(pb);
        return ResponseEntity.ok(pb);
    }

    // ---------------- CONSUME BONUS ----------------
    @PostMapping("/players/{playerId}/consume")
    public ResponseEntity<?> consumeBonus(@PathVariable String playerId) {

        List<PlayerBonus> list = playerBonuses.get(playerId);

        if (list == null || list.isEmpty()) {
            return ResponseEntity.status(404).body("No bonuses");
        }

        for (Iterator<PlayerBonus> it = list.iterator(); it.hasNext();) {
            PlayerBonus b = it.next();

            if (b.isActive() && "free-spin".equals(b.getBonusId())) {
                it.remove();
                return ResponseEntity.ok("FREE SPIN CONSUMED");
            }
        }

        return ResponseEntity.status(404).body("No active free spin");
    }

    // ---------------- DTOs ----------------
    public static class CreateBonusRequest {
        public String name;
        public String description;
        public double wageringRequirement;
    }

    public static class AssignBonusRequest {
        public String playerId;
    }
}