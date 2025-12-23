package com.example.transactions.domain.repository;

import com.example.transactions.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  Optional<Transaction> findByAccount_IdAndIdempotencyKey(long accountId, String idempotencyKey);
}