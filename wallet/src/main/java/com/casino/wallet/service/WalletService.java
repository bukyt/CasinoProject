package com.casino.wallet.service;

import com.casino.wallet.dto.WalletResponse;
import com.casino.wallet.exception.InsufficientFundsException;
import com.casino.wallet.exception.InvalidWalletAmountException;
import com.casino.wallet.exception.WalletAlreadyExistsException;
import com.casino.wallet.exception.WalletNotFoundException;
import com.casino.wallet.model.Wallet;
import com.casino.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class WalletService {

    private static final BigDecimal ZERO_BALANCE = new BigDecimal("0.00");

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Integer playerProfileId) {
        return toResponse(findWallet(playerProfileId));
    }

    @Transactional
    public WalletResponse createWallet(Integer playerProfileId) {
        if (walletRepository.existsById(playerProfileId)) {
            throw new WalletAlreadyExistsException(playerProfileId);
        }

        Wallet wallet = walletRepository.save(new Wallet(playerProfileId, ZERO_BALANCE));
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse debit(Integer playerProfileId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAndValidateAmount(amount);
        Wallet wallet = findWalletForUpdate(playerProfileId);
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(normalizedAmount));
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse credit(Integer playerProfileId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAndValidateAmount(amount);
        Wallet wallet = findWalletForUpdate(playerProfileId);

        if (wallet.getAvailableBalance().compareTo(normalizedAmount) < 0) {
            throw new InsufficientFundsException(playerProfileId);
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(normalizedAmount));
        return toResponse(wallet);
    }

    private Wallet findWallet(Integer playerProfileId) {
        return walletRepository.findById(playerProfileId)
                .orElseThrow(() -> new WalletNotFoundException(playerProfileId));
    }

    private Wallet findWalletForUpdate(Integer playerProfileId) {
        return walletRepository.findByPlayerProfileIdForUpdate(playerProfileId)
                .orElseThrow(() -> new WalletNotFoundException(playerProfileId));
    }

    private BigDecimal normalizeAndValidateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWalletAmountException();
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(wallet.getPlayerProfileId(), wallet.getAvailableBalance());
    }
}
