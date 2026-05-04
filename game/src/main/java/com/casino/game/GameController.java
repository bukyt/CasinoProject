package com.casino.game;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService service;
    private final AtomicLong counter = new AtomicLong();

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listGames() {
        return ResponseEntity.ok(service.listGames());
    }

    @PostMapping("/sessions")
    public ResponseEntity<GameSession> createSession(@RequestBody CreateSessionRequest request) {
        String id = "session-" + counter.incrementAndGet();
        return ResponseEntity.ok(
                service.createSession(id, request.getGameId(), request.getInitialBalance(), request.getPlayerProfileId())
        );
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<GameSession> getSession(@PathVariable String id) {
        return service.getSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/bets")
    public ResponseEntity<?> placeBet(@PathVariable String id, @RequestBody PlaceBetRequest request) {
        try {
            return ResponseEntity.ok(service.placeBet(id, request.getAmount()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/sessions/{id}/close")
    public ResponseEntity<GameSession> closeSession(@PathVariable String id) {
        return ResponseEntity.ok(service.closeSession(id));
    }
}