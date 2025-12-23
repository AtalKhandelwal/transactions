package com.example.transactions.api.v1.controller;


import com.example.transactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionsController.class)
class TransactionsControllerTest {

  @Autowired MockMvc mvc;
  @MockBean TransactionService transactionService;

  @Test
  void createTransaction_requiresIdempotencyKey() throws Exception {
    mvc.perform(post("/v1/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"account_id\":1,\"operation_type_id\":4,\"amount\":10.00}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createTransaction_201() throws Exception {
    TransactionService.TransactionView view = new TransactionService.TransactionView(
        9L,
        1L,
        4,
        new BigDecimal("10.00"),
        Instant.parse("2020-01-01T00:00:00Z")
    );

    when(transactionService.createTransaction(eq(1L), eq(4), any(BigDecimal.class), eq("idem-1")))
        .thenReturn(new TransactionService.Result(view, false));

    mvc.perform(post("/v1/transactions")
            .header("Idempotency-Key", "idem-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"account_id\":1,\"operation_type_id\":4,\"amount\":10.00}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.transaction_id").value(9))
        .andExpect(jsonPath("$.amount").value(10.00));
  }
}