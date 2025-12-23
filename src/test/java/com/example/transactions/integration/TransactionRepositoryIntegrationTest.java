package com.example.transactions.integration;
import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class TransactionRepositoryIntegrationTest extends PostgresIntegrationTestBase {

  @Autowired JdbcTemplate jdbc;
  @Autowired AccountRepository accountRepo;
  @Autowired TransactionRepository txRepo;

  @BeforeEach
  void cleanDb() {
    jdbc.execute("TRUNCATE TABLE transactions RESTART IDENTITY CASCADE");
    jdbc.execute("TRUNCATE TABLE accounts RESTART IDENTITY CASCADE");
  }

  @Test
  void insertIfAbsent_isIdempotentForSameAccountAndKey() {
    Account acc = accountRepo.save(new Account("12345678900"));

    int first = txRepo.insertIfAbsent(
        acc.getId(),
        4,
        new BigDecimal("10.00"),
        "idem-1",
        "hash-1"
    );
    int second = txRepo.insertIfAbsent(
        acc.getId(),
        4,
        new BigDecimal("10.00"),
        "idem-1",
        "hash-1"
    );

    assertThat(first).isEqualTo(1);
    assertThat(second).isEqualTo(0);

    var tx = txRepo.findByAccount_IdAndIdempotencyKey(acc.getId(), "idem-1").orElseThrow();
    assertThat(tx.getAmount()).isEqualByComparingTo("10.00");
    assertThat(tx.getRequestHash()).isEqualTo("hash-1");
    assertThat(tx.getEventDate()).isNotNull();
  }

  @Test
  void checkConstraint_rejectsInvalidAmountSign() {
    Account acc = accountRepo.save(new Account("12345678900"));

    assertThatThrownBy(() -> txRepo.insertIfAbsent(
        acc.getId(),
        1,
        new BigDecimal("10.00"), 
        "idem-2",
        "hash-2"
    )).isInstanceOf(DataIntegrityViolationException.class);
  }
}