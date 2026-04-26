package com.casino.game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/games")
public class GameController {

    private final Map<String, Game> games = new HashMap<>();
    private final Map<String, GameSession> sessions = new HashMap<>();
    private final AtomicLong sessionIdCounter = new AtomicLong(1);

    public GameController() {
        // Initialize with slot machine game
        games.put("slot-machine", new Game("slot-machine", "Slot Machine", "Simple 3-reel slot machine"));
    }

    @GetMapping
    public ResponseEntity<List<Game>> listGames() {
        return ResponseEntity.ok(new ArrayList<>(games.values()));
    }

    @PostMapping("/sessions")
    public ResponseEntity<GameSession> createSession(@RequestBody CreateSessionRequest request) {
        String sessionId = "session-" + sessionIdCounter.getAndIncrement();
        GameSession session = new GameSession(sessionId, request.getGameId(), request.getInitialBalance());
        sessions.put(sessionId, session);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<GameSession> getSession(@PathVariable String id) {
        GameSession session = sessions.get(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/bets")
    public ResponseEntity<Bet> placeBet(@PathVariable String id, @RequestBody PlaceBetRequest request) {
        GameSession session = sessions.get(id);
        if (session == null || !"active".equals(session.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        if (session.getBalance() < request.getAmount()) {
            return ResponseEntity.badRequest().build(); // Insufficient balance
        }

        // Simulate slot machine: 50% win chance
        boolean win = new Random().nextBoolean();
        double payout = 0;
        if (win) {
            payout = request.getAmount() * 2; // 2x payout
            session.setBalance(session.getBalance() + payout - request.getAmount());
        } else {
            session.setBalance(session.getBalance() - request.getAmount());
        }

        Bet bet = new Bet(request.getAmount(), win ? "win" : "loss", payout);
        session.getBets().add(bet);
        return ResponseEntity.ok(bet);
    }

    @PatchMapping("/sessions/{id}/close")
    public ResponseEntity<GameSession> closeSession(@PathVariable String id) {
        GameSession session = sessions.get(id);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        session.setStatus("closed");
        return ResponseEntity.ok(session);
    }

    // Request classes
    public static class CreateSessionRequest {
        private String gameId;
        private double initialBalance;

        public String getGameId() { return gameId; }
        public void setGameId(String gameId) { this.gameId = gameId; }
        public double getInitialBalance() { return initialBalance; }
        public void setInitialBalance(double initialBalance) { this.initialBalance = initialBalance; }
    }

    public static class PlaceBetRequest {
        private double amount;

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}