package com.example.transactions.service;

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

  private final TransactionCommandService commandService;

  public TransactionService(TransactionCommandService commandService) {
    this.commandService = commandService;
  }

  
  public Result createTransaction(long accountId, int operationTypeId, BigDecimal clientAmount, String idempotencyKey) {
    return commandService.createTransactionTx(accountId, operationTypeId, clientAmount, idempotencyKey);
  }
}
