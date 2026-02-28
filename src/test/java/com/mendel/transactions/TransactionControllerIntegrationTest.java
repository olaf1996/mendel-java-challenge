package com.mendel.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @DisplayName("PUT /transactions/{id}")
    class PutTransaction {

        @Test
        @DisplayName("crear transacción sin parent devuelve 200 y status ok")
        void createWithoutParent_returns200AndStatusOk() throws Exception {
            String body = """
                    {"amount": 5000, "type": "cars"}
                    """;
            mockMvc.perform(put("/transactions/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
        }

        @Test
        @DisplayName("crear transacción con parent existente devuelve 200 y status ok")
        void createWithExistingParent_returns200AndStatusOk() throws Exception {
            // Primero crear la transacción 10 (parent)
            String parentBody = """
                    {"amount": 5000, "type": "cars"}
                    """;
            mockMvc.perform(put("/transactions/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(parentBody))
                    .andExpect(status().isOk());

            // Luego crear la 11 con parent_id 10
            String childBody = """
                    {"amount": 10000, "type": "shopping", "parent_id": 10}
                    """;
            mockMvc.perform(put("/transactions/11")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(childBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
        }
    }

    @Nested
    @DisplayName("GET /transactions/types/{type}")
    class GetTransactionsByType {

        @Test
        @DisplayName("sin transacciones del tipo devuelve 200 y lista vacía")
        void noTransactionsForType_returns200AndEmptyList() throws Exception {
            mockMvc.perform(get("/transactions/types/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("con transacciones del tipo devuelve 200 y lista de ids")
        void withTransactionsForType_returns200AndListOfIds() throws Exception {
            mockMvc.perform(put("/transactions/20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"food\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/transactions/21")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 200, \"type\": \"food\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/transactions/types/food"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$").value(hasItems(20, 21)));
        }
    }
}
