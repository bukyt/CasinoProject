package com.casino.game;

import com.casino.event.GameEndingType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Test
    void placeBet_success() throws Exception {
        Bet bet = new Bet();
        // Change these two lines:
        bet.setAmount(BigDecimal.valueOf(10));
        bet.setOutcome(GameEndingType.WIN);
        bet.setPayout(BigDecimal.valueOf(20));

        when(gameService.placeBet("session-1", 10)).thenReturn(bet);

        mockMvc.perform(post("/games/sessions/session-1/bets")
                        .contentType("application/json")
                        .content("{\"amount\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("WIN"));
    }

    @Test
    void placeBet_insufficientFunds() throws Exception {
        when(gameService.placeBet("session-1", 10))
                .thenThrow(new RuntimeException("Insufficient funds"));

        mockMvc.perform(post("/games/sessions/session-1/bets")
                        .contentType("application/json")
                        .content("{\"amount\":10}"))
                .andExpect(status().isBadRequest());
    }
}