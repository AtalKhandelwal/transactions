package com.example.transactions.integration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
public class MigrationVerificationIntegrationTest extends PostgresIntegrationTestBase {
    @Autowired JdbcTemplate jdbc;

  @Test
  void flywayRanAndCreatedTables() {
    Integer applied = jdbc.queryForObject(
        "select count(*) from flyway_schema_history where success = true",
        Integer.class
    );
    assertThat(applied).isNotNull();
    assertThat(applied).isGreaterThanOrEqualTo(1);

    Integer accountsExists = jdbc.queryForObject(
        "select count(*) from information_schema.tables where table_name = 'accounts'",
        Integer.class
    );
    Integer txExists = jdbc.queryForObject(
        "select count(*) from information_schema.tables where table_name = 'transactions'",
        Integer.class
    );

    assertThat(accountsExists).isEqualTo(1);
    assertThat(txExists).isEqualTo(1);
  }

  @Test
  void constraintsExist() {
    List<String> accountConstraints = jdbc.queryForList(
        "select conname from pg_constraint where conrelid = 'accounts'::regclass",
        String.class
    );
    assertThat(accountConstraints).contains("uk_accounts_document_number");

    List<String> txConstraints = jdbc.queryForList(
        "select conname from pg_constraint where conrelid = 'transactions'::regclass",
        String.class
    );
    assertThat(txConstraints).contains(
        "fk_transactions_account",
        "uk_transactions_idempotency",
        "chk_transactions_amount_sign"
    );
  }

  @Test
  void defaultsExistForTimestamps() {
    String accCreatedDefault = jdbc.queryForObject(
        "select column_default from information_schema.columns where table_name='accounts' and column_name='created_at'",
        String.class
    );
    String txEventDefault = jdbc.queryForObject(
        "select column_default from information_schema.columns where table_name='transactions' and column_name='event_date'",
        String.class
    );

    assertThat(accCreatedDefault).containsIgnoringCase("now()");
    assertThat(txEventDefault).containsIgnoringCase("now()");
  }
}
