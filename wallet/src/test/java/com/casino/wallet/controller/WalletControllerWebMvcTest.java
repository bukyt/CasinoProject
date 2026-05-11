package com.casino.wallet.controller;

import com.casino.wallet.dto.WalletResponse;
import com.casino.wallet.exception.InsufficientFundsException;
import com.casino.wallet.exception.InvalidWalletAmountException;
import com.casino.wallet.exception.WalletNotFoundException;
import com.casino.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Test
    void getWalletReturnsOkWhenWalletExists() throws Exception {
        when(walletService.getWallet(101))
                .thenReturn(new WalletResponse(101, new BigDecimal("15.00")));

        mockMvc.perform(get("/wallet/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerProfileId").value(101))
                .andExpect(jsonPath("$.availableBalance").value(15.00));
    }

    @Test
    void getWalletReturnsNotFoundWhenWalletMissing() throws Exception {
        when(walletService.getWallet(999))
                .thenThrow(new WalletNotFoundException(999));

        mockMvc.perform(get("/wallet/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Wallet not found for playerProfileId 999"));
    }

    @Test
    void createWalletReturnsBadRequestWhenWalletExists() throws Exception {
        when(walletService.createWallet(101))
                .thenThrow(new com.casino.wallet.exception.WalletAlreadyExistsException(101));

        mockMvc.perform(post("/wallet/create/101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void debitReturnsUpdatedBalance() throws Exception {
        when(walletService.debit(101, new BigDecimal("5.00")))
                .thenReturn(new WalletResponse(101, new BigDecimal("8.00")));

        mockMvc.perform(post("/wallet/debit/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableBalance").value(8.00));
    }

    @Test
    void creditReturnsBadRequestForInsufficientFunds() throws Exception {
        when(walletService.credit(101, new BigDecimal("50.00")))
                .thenThrow(new InsufficientFundsException(101));

        mockMvc.perform(post("/wallet/credit/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":50.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds for playerProfileId 101"));
    }

    @Test
    void debitReturnsBadRequestForInvalidAmount() throws Exception {
        when(walletService.debit(101, BigDecimal.ZERO))
                .thenThrow(new InvalidWalletAmountException());

        mockMvc.perform(post("/wallet/debit/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than zero"));
    }
}
