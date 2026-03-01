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

import static org.hamcrest.Matchers.containsString;
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

        @Test
        @DisplayName("parent_id inexistente devuelve 400")
        void parentIdNotExist_returns400() throws Exception {
            mockMvc.perform(put("/transactions/40")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"x\", \"parent_id\": 99999}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Parent transaction does not exist")));
        }

        @Test
        @DisplayName("parent_id que formaría ciclo devuelve 400")
        void parentIdWouldCreateCycle_returns400() throws Exception {
            mockMvc.perform(put("/transactions/50")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"a\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/transactions/51")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 200, \"type\": \"a\", \"parent_id\": 50}"))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/transactions/50")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"a\", \"parent_id\": 51}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Cycle")));
        }

        @Test
        @DisplayName("type vacío devuelve 400")
        void typeBlank_returns400() throws Exception {
            mockMvc.perform(put("/transactions/60")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("amount faltante devuelve 400")
        void amountMissing_returns400() throws Exception {
            mockMvc.perform(put("/transactions/61")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\": \"valid\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("type faltante devuelve 400")
        void typeMissing_returns400() throws Exception {
            mockMvc.perform(put("/transactions/62")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("JSON mal formado devuelve 400")
        void invalidJson_returns400() throws Exception {
            mockMvc.perform(put("/transactions/63")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid JSON")));
        }

        @Test
        @DisplayName("transaction_id en path no numérico devuelve 400")
        void transactionIdNotNumeric_returns400() throws Exception {
            mockMvc.perform(put("/transactions/abc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"x\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("must be a valid number")));
        }

        @Test
        @DisplayName("transaction_id negativo devuelve 400")
        void transactionIdNegative_returns400() throws Exception {
            mockMvc.perform(put("/transactions/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"x\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("non-negative")));
        }

        @Test
        @DisplayName("parent_id negativo devuelve 400")
        void parentIdNegative_returns400() throws Exception {
            mockMvc.perform(put("/transactions/64")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"x\", \"parent_id\": -1}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("parent_id")));
        }

        @Test
        @DisplayName("amount NaN (JSON no estándar) devuelve 400")
        void amountNaN_returns400() throws Exception {
            mockMvc.perform(put("/transactions/65")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": NaN, \"type\": \"x\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid JSON")));
        }

        @Test
        @DisplayName("type solo espacios devuelve 400")
        void typeWhitespaceOnly_returns400() throws Exception {
            mockMvc.perform(put("/transactions/66")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 100, \"type\": \"   \"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
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

    @Nested
    @DisplayName("GET /transactions/sum/{id}")
    class GetTransactionSum {

        @Test
        @DisplayName("suma transitiva: caso challenge 10->11->12, sum/10=20000 y sum/11=15000")
        void transitiveSum_returnsSumOfTransactionAndDescendants() throws Exception {
            mockMvc.perform(put("/transactions/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 5000, \"type\": \"cars\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/transactions/11")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 10000, \"type\": \"shopping\", \"parent_id\": 10}"))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/transactions/12")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 5000, \"type\": \"shopping\", \"parent_id\": 11}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/transactions/sum/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(20000.0));
            mockMvc.perform(get("/transactions/sum/11"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(15000.0));
        }

        @Test
        @DisplayName("id inexistente devuelve 404")
        void nonExistentId_returns404() throws Exception {
            mockMvc.perform(get("/transactions/sum/99999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("transacción sin hijos devuelve solo su monto")
        void transactionWithNoChildren_returnsOwnAmount() throws Exception {
            mockMvc.perform(put("/transactions/70")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"amount\": 333.5, \"type\": \"solo\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/transactions/sum/70"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sum").value(333.5));
        }

        @Test
        @DisplayName("transaction_id en path no numérico devuelve 400")
        void sumIdNotNumeric_returns400() throws Exception {
            mockMvc.perform(get("/transactions/sum/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("must be a valid number")));
        }
    }
}
