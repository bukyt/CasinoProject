package com.casino.bonus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BonusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateBonus() throws Exception {
        mockMvc.perform(post("/bonuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Welcome Bonus",
                          "description": "100% match bonus",
                          "wageringRequirement": 1000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldListBonuses() throws Exception {
        mockMvc.perform(get("/bonuses"))
                .andExpect(status().isOk());
    }
}