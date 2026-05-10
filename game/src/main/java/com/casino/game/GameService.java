package com.casino.game;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;

import com.casino.event.BetPlaced;
import com.casino.event.BetSettled;
import com.casino.event.GameEndingType;

@Service
public class GameService {

    private final GameSessionRepository repository;
    private final GameEventProducer eventProducer;
    private final GameRepository gameRepository;
    private final RestTemplate restTemplate;
    private final Random random = new Random();

    public GameService(
            GameSessionRepository repository,
            GameEventProducer eventProducer,
            GameRepository gameRepository,
            RestTemplate restTemplate
    ) {
        this.repository = repository;
        this.eventProducer = eventProducer;
        this.gameRepository = gameRepository;
        this.restTemplate = restTemplate;
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
        return gameRepository.findAll();
    }

    @Transactional
    public Bet placeBet(String sessionId, double amount) {

        GameSession session = repository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"active".equals(session.getStatus())) {
            throw new RuntimeException("Session closed");
        }

        // IMPORTANT: normalize type for bonus service
        String playerId = String.valueOf(session.getPlayerProfileId());

        //
        // CHECK BONUS
        //
        Boolean hasBonus = false;

        try {
            hasBonus = restTemplate.getForObject(
                    "http://localhost:8084/bonuses/players/" +
                            playerId +
                            "/has-active-bonus",
                    Boolean.class
            );
        } catch (Exception e) {
            System.err.println("BONUS CHECK FAILED (continuing game)");
        }

        boolean freeSpin = Boolean.TRUE.equals(hasBonus);

        //
        // CONSUME BONUS
        //
        if (freeSpin) {
            try {
                restTemplate.exchange(
                        "http://localhost:8084/bonuses/players/" +
                                playerId +
                                "/consume",
                        HttpMethod.PATCH,
                        null,
                        Object.class
                );

                amount = 0;

                System.out.println("FREE SPIN USED");

            } catch (Exception e) {
                System.err.println("BONUS CONSUME FAILED (ignored)");
            }
        }

        //
        // BALANCE CHECK
        //
        if (session.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }

        //
        // RANDOM BONUS (20%)
        //
        if (random.nextInt(5) == 0) {
            try {
                restTemplate.postForObject(
                        "http://localhost:8084/bonuses/players/" +
                                playerId +
                                "/grant-free-spin",
                        null,
                        Object.class
                );

                System.out.println("FREE SPIN AWARDED");
                System.out.println(playerId.toString());

            } catch (Exception e) {
                System.err.println("BONUS GRANT FAILED");
            }
        }

        //
        // KAFKA: BET PLACED
        //
        try {
            BetPlaced placed = new BetPlaced();
            placed.setPlayerProfileId(Integer.valueOf(playerId));
            placed.setAmount(BigDecimal.valueOf(amount));
            eventProducer.sendBetPlaced(placed);

        } catch (Exception e) {
            System.err.println("KAFKA ERROR (BetPlaced)");
        }

        //
        // GAME LOGIC
        //
        boolean win = random.nextDouble() < 0.3;

        double payout = 0;

        if (win) {
            payout = amount * 2;

            session.setBalance(
                    session.getBalance() + payout - amount
            );
        } else {
            session.setBalance(
                    session.getBalance() - amount
            );
        }

        //
        // BET ENTITY
        //
        Bet bet = new Bet();
        bet.setAmount(BigDecimal.valueOf(amount));
        bet.setPayout(BigDecimal.valueOf(payout));
        bet.setOutcome(win ? GameEndingType.WIN : GameEndingType.LOSE);

        session.getBets().add(bet);

        //
        // KAFKA: SETTLED
        //
        try {
            BetSettled settled = new BetSettled();
            settled.setPlayerProfileId(Integer.valueOf(playerId));
            settled.setAmount(BigDecimal.valueOf(amount));
            settled.setGameEndingType(
                    win ? GameEndingType.WIN : GameEndingType.LOSE
            );

            eventProducer.sendBetSettled(settled);

        } catch (Exception e) {
            System.err.println("KAFKA ERROR (BetSettled)");
        }

        repository.save(session);

        return bet;
    }

    public GameSession closeSession(String id) {
        GameSession session = repository.findById(id)
                .orElseThrow();

        session.setStatus("closed");
        return repository.save(session);
    }
}