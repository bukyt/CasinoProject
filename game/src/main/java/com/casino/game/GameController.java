package com.casino.game;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/games")
@Tag(name = "Game Service", description = "Endpoints for managing game sessions and placing bets")
public class GameController {

    private final GameService service;
    private final AtomicLong counter = new AtomicLong();

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all games", description = "Returns a list of available games in the system")
    public ResponseEntity<?> listGames() {
        return ResponseEntity.ok(service.listGames());
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create a game session", description = "Starts a new session for a specific game and player")
    public ResponseEntity<GameSession> createSession(@RequestBody CreateSessionRequest request) {

        String id = "session-" + counter.incrementAndGet();

        GameSession session = service.createSession(
                id,
                request.getGameId(),
                request.getInitialBalance(),
                request.getPlayerProfileId()
        );

        // DEBUG (important for your current issue)
        System.out.println("CREATED SESSION ID: " + session.getId());

        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "Get session details", description = "Fetches the current state of a game session by ID")
    public ResponseEntity<GameSession> getSession(@PathVariable String id) {
        return service.getSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/bets")
    @Operation(summary = "Place a bet", description = "Places a bet within an active session and returns the result")
    @ApiResponse(responseCode = "200", description = "Bet placed successfully")
    @ApiResponse(responseCode = "400", description = "Insufficient funds or closed session")
    public ResponseEntity<?> placeBet(
            @Parameter(description = "The session ID") @PathVariable String id,
            @RequestBody PlaceBetRequest request) {

        try {
            return ResponseEntity.ok(service.placeBet(id, request.getAmount()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/sessions/{id}/close")
    @Operation(summary = "Close session", description = "Ends the game session and prevents further bets")
    public ResponseEntity<GameSession> closeSession(@PathVariable String id) {
        return ResponseEntity.ok(service.closeSession(id));
    }
}