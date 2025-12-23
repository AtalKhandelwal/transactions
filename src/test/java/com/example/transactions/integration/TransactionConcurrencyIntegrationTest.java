package com.example.transactions.integration;

import com.example.transactions.service.AccountService;
import com.example.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionConcurrencyIntegrationTest extends PostgresIntegrationTestBase {

  @Autowired JdbcTemplate jdbc;
  @Autowired AccountService accountService;
  @Autowired TransactionService transactionService;

  @BeforeEach
  void cleanDb() {
    jdbc.execute("TRUNCATE TABLE transactions RESTART IDENTITY CASCADE");
    jdbc.execute("TRUNCATE TABLE accounts RESTART IDENTITY CASCADE");
  }

  @Test
  void concurrentCreateTransaction_sameIdempotencyKey_createsSingleRow() throws Exception {
    var acc = accountService.createAccount("12345678900");

    int n = 12;
    ExecutorService pool = Executors.newFixedThreadPool(n);

    CountDownLatch ready = new CountDownLatch(n);
    CountDownLatch start = new CountDownLatch(1);

    List<Future<TransactionService.Result>> futures = new ArrayList<>();

    for (int i = 0; i < n; i++) {
      futures.add(pool.submit(() -> {
        ready.countDown();
        start.await(5, TimeUnit.SECONDS);
        return transactionService.createTransaction(
            acc.getId(), 4, new BigDecimal("10.00"), "idem-1"
        );
      }));
    }

    assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
    start.countDown();

    List<TransactionService.Result> results = new ArrayList<>();
    for (Future<TransactionService.Result> f : futures) {
      results.add(f.get(10, TimeUnit.SECONDS));
    }

    pool.shutdownNow();

    long txId = results.get(0).transaction().transactionId();
    long notReplayed = results.stream().filter(r -> !r.replayed()).count();
    long replayed = results.stream().filter(TransactionService.Result::replayed).count();

    assertThat(results).allSatisfy(r -> assertThat(r.transaction().transactionId()).isEqualTo(txId));
    assertThat(notReplayed).isEqualTo(1);
    assertThat(replayed).isEqualTo(n - 1);

    Integer count = jdbc.queryForObject(
        "select count(*) from transactions where account_id = ? and idempotency_key = ?",
        Integer.class,
        acc.getId(), "idem-1"
    );
    assertThat(count).isEqualTo(1);
  }
}
