package com.example.transactions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransactionService {

  public record TransactionView(
      long transactionId,
      long accountId,
      int operationTypeId,
      BigDecimal amount,
      Instant eventDate) {
  }

  public record Result(TransactionView transaction, boolean replayed) {
  }

  private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
  private final TransactionCommandService commandService;

  public TransactionService(TransactionCommandService commandService) {
    this.commandService = commandService;
  }

  @Retryable(
      retryFor = TransientDataAccessException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 200, multiplier = 2.0, random = true)
  )
  public Result createTransaction(long accountId, int operationTypeId, BigDecimal clientAmount, String idempotencyKey) {
    log.debug("create_transaction_attempt accountId={} opTypeId={}", accountId, operationTypeId);
    return commandService.createTransactionTx(accountId, operationTypeId, clientAmount, idempotencyKey);
  }
}
