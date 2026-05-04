package com.casino.game;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.casino.event.BetPlaced;
import com.casino.event.BetSettled;
import com.casino.event.GameEndingType;

@Service
public class GameService {

    private final GameSessionRepository repository;
    private final GameEventProducer eventProducer;

    public GameService(GameSessionRepository repository, GameEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    public GameSession createSession(String id, String gameId, double balance, Integer playerProfileId) {
        GameSession session = new GameSession();
        session.setId(id);
        session.setGameId(gameId);
        session.setBalance(balance);
        session.setStatus("active");
        session.setPlayerProfileId(playerProfileId);
        return repository.save(session);
    }

    public Optional<GameSession> getSession(String id) {
        return repository.findById(id);
    }

    public List<Game> listGames() {
        return List.of(
                new Game("slot-machine", "Slot Machine", "Simple 3-reel slot machine")
        );
    }
    
    @Transactional
    public Bet placeBet(String sessionId, double amount) {

        GameSession session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("Session closed");
        }

        if (session.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }

        try {
            BetPlaced placed = new BetPlaced();
            placed.setPlayerProfileId(session.getPlayerProfileId());
            placed.setAmount(BigDecimal.valueOf(amount));
            eventProducer.sendBetPlaced(placed);
        } catch (Exception e) {
            System.err.println("KAFKA ERROR: Could not send BetPlaced event, but continuing game.");
        }

        boolean win = new Random().nextBoolean();

        double payout = 0;

        if (win) {
            payout = amount * 2;
            session.setBalance(session.getBalance() + payout - amount);
        } else {
            session.setBalance(session.getBalance() - amount);
        }

        Bet bet = new Bet();
        bet.setAmount(BigDecimal.valueOf(amount));
        bet.setPayout(BigDecimal.valueOf(payout));
        bet.setOutcome(win ? GameEndingType.WIN : GameEndingType.LOSE);
        // ADD THIS LINE if Bet has a gameSession field:
        // bet.setGameSession(session); 
        session.getBets().add(bet);
        try {
            BetSettled settled = new BetSettled();
            settled.setPlayerProfileId(session.getPlayerProfileId());
            // FIX: Convert double to BigDecimal
            settled.setAmount(BigDecimal.valueOf(amount));
            settled.setGameEndingType(win ? GameEndingType.WIN : GameEndingType.LOSE);
            eventProducer.sendBetSettled(settled);
        } catch (Exception e) {
            System.err.println("KAFKA OFFLINE: Skipping BetSettled event.");
        }
        repository.save(session);

        // 2. Return the 'bet' object to satisfy the method's return type
        return bet;
    }

    public GameSession closeSession(String id) {
        GameSession session = repository.findById(id)
                .orElseThrow();

        session.setStatus("closed");
        return repository.save(session);
    }
}