package com.casino.wallet;

import com.casino.wallet.dto.WalletResponse;
import com.casino.wallet.exception.InsufficientFundsException;
import com.casino.wallet.exception.InvalidWalletAmountException;
import com.casino.wallet.exception.WalletNotFoundException;
import com.casino.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WalletApplicationTests {

    @Autowired
    private WalletService walletService;

    @Test
    void contextLoads() {
    }

    @Test
    void getExistingWalletReturnsBalance() {
        walletService.createWallet(101);
        walletService.debit(101, new BigDecimal("25.50"));

        WalletResponse response = walletService.getWallet(101);

        assertEquals(101, response.playerProfileId());
        assertEquals(new BigDecimal("25.50"), response.availableBalance());
    }

    @Test
    void getDebitAndCreditFailWhenWalletDoesNotExist() {
        assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(99901));
        assertThrows(WalletNotFoundException.class, () -> walletService.debit(99901, BigDecimal.ONE));
        assertThrows(WalletNotFoundException.class, () -> walletService.credit(99901, BigDecimal.ONE));
    }

    @Test
    void debitIncreasesBalance() {
        walletService.createWallet(102);

        WalletResponse response = walletService.debit(102, new BigDecimal("10.25"));

        assertEquals(new BigDecimal("10.25"), response.availableBalance());
    }

    @Test
    void creditDecreasesBalance() {
        walletService.createWallet(103);
        walletService.debit(103, new BigDecimal("20.00"));

        WalletResponse response = walletService.credit(103, new BigDecimal("7.25"));

        assertEquals(new BigDecimal("12.75"), response.availableBalance());
    }

    @Test
    void creditRejectsInsufficientFunds() {
        walletService.createWallet(104);

        assertThrows(InsufficientFundsException.class,
                () -> walletService.credit(104, new BigDecimal("0.01")));
    }

    @Test
    void zeroOrNegativeAmountsAreRejected() {
        walletService.createWallet(105);

        assertThrows(InvalidWalletAmountException.class,
                () -> walletService.debit(105, BigDecimal.ZERO));
        assertThrows(InvalidWalletAmountException.class,
                () -> walletService.credit(105, new BigDecimal("-1.00")));
    }

    @Test
    void concurrentDebitsProduceCorrectFinalBalance() throws Exception {
        walletService.createWallet(106);

        runInParallel(12, () -> walletService.debit(106, BigDecimal.ONE));

        WalletResponse response = walletService.getWallet(106);
        assertEquals(new BigDecimal("12.00"), response.availableBalance());
    }

    @Test
    void concurrentCreditsCannotOverdrawPastAvailableFunds() throws Exception {
        walletService.createWallet(107);
        walletService.debit(107, new BigDecimal("100.00"));

        List<Object> outcomes = runInParallel(10,
                () -> walletService.credit(107, new BigDecimal("15.00")));

        long successCount = outcomes.stream().filter(WalletResponse.class::isInstance).count();
        long insufficientFundsCount = outcomes.stream()
                .filter(InsufficientFundsException.class::isInstance)
                .count();

        WalletResponse response = walletService.getWallet(107);

        assertEquals(6, successCount);
        assertEquals(4, insufficientFundsCount);
        assertEquals(new BigDecimal("10.00"), response.availableBalance());
    }

    @Test
    void simultaneousCreditAndDebitDoNotLoseUpdates() throws Exception {
        walletService.createWallet(108);
        walletService.debit(108, new BigDecimal("50.00"));

        List<Callable<Object>> tasks = new ArrayList<>();
        for (int index = 0; index < 20; index++) {
            tasks.add(() -> walletService.debit(108, new BigDecimal("5.00")));
        }
        for (int index = 0; index < 10; index++) {
            tasks.add(() -> walletService.credit(108, new BigDecimal("3.00")));
        }

        runInParallel(tasks);

        WalletResponse response = walletService.getWallet(108);
        assertEquals(new BigDecimal("120.00"), response.availableBalance());
    }

    private List<Object> runInParallel(int taskCount, Callable<Object> task) throws Exception {
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int index = 0; index < taskCount; index++) {
            tasks.add(task);
        }
        return runInParallel(tasks);
    }

    private List<Object> runInParallel(List<Callable<Object>> tasks) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (Callable<Object> task : tasks) {
                futures.add(executorService.submit(() -> {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    try {
                        return task.call();
                    } catch (Exception exception) {
                        return unwrapExecutionException(exception);
                    }
                }));
            }

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();

            List<Object> results = new ArrayList<>();
            for (Future<Object> future : futures) {
                results.add(future.get(10, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executorService.shutdownNow();
        }
    }

    private Throwable unwrapExecutionException(Exception exception) {
        if (exception instanceof ExecutionException executionException && executionException.getCause() != null) {
            return executionException.getCause();
        }
        return exception;
    }
}
