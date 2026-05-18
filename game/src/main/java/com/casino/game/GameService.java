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
import com.casino.game.dto.wallet.WalletResponse;

@Service
public class GameService {

    private final GameSessionRepository repository;
    private final GameEventProducer eventProducer;
    private final GameRepository gameRepository;
    private final BonusClient bonusClient;
    private final WalletClient walletClient; // ADDED: Wallet client integration
    private final Random random = new Random();

    public GameService(
            GameSessionRepository repository,
            GameEventProducer eventProducer,
            GameRepository gameRepository,
            BonusClient bonusClient,
            WalletClient walletClient // ADDED: Injected via constructor
    ) {
        this.repository = repository;
        this.eventProducer = eventProducer;
        this.gameRepository = gameRepository;
        this.bonusClient = bonusClient;
        this.walletClient = walletClient;
    }

    public GameSession createSession(String id, String gameId, double balance, Integer playerProfileId) {
        GameSession session = new GameSession();
        session.setId(id);
        session.setGameId(gameId);
        session.setStatus("active");
        session.setPlayerProfileId(playerProfileId);

        // WALLET FLOW: Fetch or create a wallet automatically on startup
        Long profileIdLong = Long.valueOf(playerProfileId);
        try {
            WalletResponse wallet = walletClient.getWallet(profileIdLong);
            if (wallet == null) {
                System.out.println("Wallet not found for player: " + playerProfileId + ". Auto-creating wallet...");
                wallet = walletClient.createWallet(profileIdLong);
            }
            
            // If the wallet exists or was successfully created, use its live balance as the source of truth
            if (wallet != null && wallet.availableBalance() != null) {
                balance = wallet.availableBalance().doubleValue();
            }
        } catch (Exception e) {
            System.err.println("WALLET SERVICE UNREACHABLE: Falling back to initial balance argument.");
        }

        session.setBalance(balance);
        return repository.save(session);
    }

    public Optional<GameSession> getSession(String id) {
        return repository.findById(id).map(this::enrichSession);
    }

    private GameSession enrichSession(GameSession session) {
        String playerId = String.valueOf(session.getPlayerProfileId());

        try {
            session.setHasActiveBonus(bonusClient.hasActiveBonus(playerId));
        } catch (Exception e) {
            session.setHasActiveBonus(false);
        }

        return session;
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

        String playerId = String.valueOf(session.getPlayerProfileId());
        Long profileIdLong = Long.valueOf(session.getPlayerProfileId());

        boolean freeSpin = false;

        try {
            freeSpin = bonusClient.hasActiveBonus(playerId);
        } catch (Exception e) {
            System.err.println("BONUS CHECK FAILED (continuing game)");
        }

        if (freeSpin) {
            try {
                bonusClient.consumeBonus(playerId);
                amount = 0.0; // FREE SPIN COST IS ZERO ONLY
                System.out.println("FREE SPIN USED");
            } catch (Exception e) {
                System.err.println("BONUS CONSUME FAILED");
            }
        }

        // Check local session budget safety bounds
        if (session.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }

        // WALLET FLOW: Credit from wallet (Decreases real remote funds)
        if (amount > 0.0) {
            WalletResponse updatedWallet = walletClient.credit(profileIdLong, BigDecimal.valueOf(amount));
            if (updatedWallet == null) {
                throw new RuntimeException("Wallet transaction rejected: Insufficient funds or service down.");
            }
        }

        // award free spin randomly
        if (random.nextInt(5) == 0) {
            try {
                bonusClient.grantFreeSpin(playerId);
                System.out.println("FREE SPIN AWARDED");
            } catch (Exception e) {
                System.err.println("BONUS GRANT FAILED");
            }
        }

        // send bet event
        try {
            BetPlaced placed = new BetPlaced();
            placed.setPlayerProfileId(Integer.valueOf(playerId));
            placed.setAmount(BigDecimal.valueOf(amount));
            eventProducer.sendBetPlaced(placed);
        } catch (Exception e) {
            System.err.println("KAFKA ERROR (BetPlaced)");
        }

        boolean win = random.nextDouble() < 0.3;
        double payout = 0.0;

        if (win) {
            payout = amount + 10.0; // WINNING PAYS BET + 10

            // WALLET FLOW: Debit into wallet (Increases real remote funds for payouts)
            if (payout > 0.0) {
                walletClient.debit(profileIdLong, BigDecimal.valueOf(payout));
            }
            
            // Sync local session cache state
            session.setBalance(session.getBalance() + payout);
        }

        if (!freeSpin) {
            session.setBalance(session.getBalance() - amount);
        }

        Bet bet = new Bet();
        bet.setAmount(BigDecimal.valueOf(amount));
        bet.setPayout(BigDecimal.valueOf(payout));
        bet.setOutcome(win ? GameEndingType.WIN : GameEndingType.LOSE);

        session.getBets().add(bet);

        try {
            BetSettled settled = new BetSettled();
            settled.setPlayerProfileId(Integer.valueOf(playerId));
            settled.setAmount(BigDecimal.valueOf(amount));
            settled.setGameEndingType(win ? GameEndingType.WIN : GameEndingType.LOSE);
            eventProducer.sendBetSettled(settled);
        } catch (Exception e) {
            System.err.println("KAFKA ERROR (BetSettled)");
        }

        repository.save(session);
        return bet;
    }

    public GameSession closeSession(String id) {
        GameSession session = repository.findById(id).orElseThrow();
        session.setStatus("closed");
        return repository.save(session);
    }
}