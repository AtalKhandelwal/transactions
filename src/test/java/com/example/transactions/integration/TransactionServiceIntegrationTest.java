package com.example.transactions.integration;

import com.example.transactions.service.AccountService;
import com.example.transactions.service.TransactionService;
import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class TransactionServiceIntegrationTest extends PostgresIntegrationTestBase {

  @Autowired JdbcTemplate jdbc;
  @Autowired AccountService accountService;
  @Autowired TransactionService transactionService;

  @BeforeEach
  void cleanDb() {
    jdbc.execute("TRUNCATE TABLE transactions RESTART IDENTITY CASCADE");
    jdbc.execute("TRUNCATE TABLE accounts RESTART IDENTITY CASCADE");
  }

  @Test
  void createTransaction_insertsOnce_andReplays() {
    var acc = accountService.createAccount("12345678900");

    TransactionService.Result r1 = transactionService.createTransaction(
        acc.getId(), 4, new BigDecimal("10.00"), "idem-1"
    );
    TransactionService.Result r2 = transactionService.createTransaction(
        acc.getId(), 4, new BigDecimal("10.00"), "idem-1"
    );

    assertThat(r1.replayed()).isFalse();
    assertThat(r2.replayed()).isTrue();
    assertThat(r2.transaction().transactionId()).isEqualTo(r1.transaction().transactionId());

    Integer count = jdbc.queryForObject(
        "select count(*) from transactions where account_id = ? and idempotency_key = ?",
        Integer.class,
        acc.getId(), "idem-1"
    );
    assertThat(count).isEqualTo(1);
  }

  @Test
  void createTransaction_sameIdempotencyKeyDifferentPayload_conflicts() {
    var acc = accountService.createAccount("12345678900");

    transactionService.createTransaction(acc.getId(), 4, new BigDecimal("10.00"), "idem-1");

    assertThatThrownBy(() ->
        transactionService.createTransaction(acc.getId(), 4, new BigDecimal("11.00"), "idem-1")
    ).isInstanceOf(Exceptions.Conflict.class)
     .hasMessageContaining("Idempotency-Key already used");
  }

  @Test
  void createTransaction_invalidAmountScale_isBadRequest() {
    var acc = accountService.createAccount("12345678900");

    assertThatThrownBy(() ->
        transactionService.createTransaction(acc.getId(), 4, new BigDecimal("10.001"), "idem-1")
    ).isInstanceOf(Exceptions.BadRequest.class)
     .hasMessageContaining("at most 2 decimal places");
  }
}
