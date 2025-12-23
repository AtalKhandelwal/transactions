package com.example.transactions.domain.repository;

import com.example.transactions.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Modifying
  @Query(value = """
      INSERT INTO transactions(account_id, operation_type_id, amount, idempotency_key, request_hash)
      VALUES (:accountId, :operationTypeId, :amount, :idempotencyKey, :requestHash)
      ON CONFLICT (account_id, idempotency_key) DO NOTHING
      """, nativeQuery = true)
  int insertIfAbsent(
      @Param("accountId") long accountId,
      @Param("operationTypeId") int operationTypeId,
      @Param("amount") BigDecimal amount,
      @Param("idempotencyKey") String idempotencyKey,
      @Param("requestHash") String requestHash
  );

  Optional<Transaction> findByAccount_IdAndIdempotencyKey(long accountId, String idempotencyKey);
}