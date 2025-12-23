package com.example.transactions.service;

import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.model.OperationType;
import com.example.transactions.domain.model.Transaction;
import com.example.transactions.domain.policy.TransactionRules;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.domain.repository.TransactionRepository;
import com.example.transactions.service.hashing.IdempotencyHasher;
import com.example.transactions.support.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionCommandService {

  private static final Logger log = LoggerFactory.getLogger(TransactionCommandService.class);

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final IdempotencyHasher hasher;

  public TransactionCommandService(AccountRepository accountRepository,
                                  TransactionRepository transactionRepository,
                                  IdempotencyHasher hasher) {
    this.accountRepository = accountRepository;
    this.transactionRepository = transactionRepository;
    this.hasher = hasher;
  }

  @Transactional
  public TransactionService.Result createTransactionTx(long accountId,
                                                      int operationTypeId,
                                                      BigDecimal clientAmount,
                                                      String idempotencyKey) {

    String idem = TransactionRules.validateIdempotencyKey(idempotencyKey);
    long accId = TransactionRules.validateAccountId(accountId);
    OperationType op = TransactionRules.validateOperationType(operationTypeId);
    BigDecimal normalized = TransactionRules.normalizeClientAmount(clientAmount);
    BigDecimal storedAmount = TransactionRules.toStoredAmount(op, normalized);

    Account account = accountRepository.findById(accId)
        .orElseThrow(() -> new Exceptions.NotFound("account not found"));

    String canonical = TransactionRules.canonicalRequest(accId, operationTypeId, normalized);
    String requestHash = hasher.sha256(canonical);

    int inserted = transactionRepository.insertIfAbsent(
        accId,
        operationTypeId,
        storedAmount,
        idem,
        requestHash
    );

    Transaction existing = transactionRepository.findByAccount_IdAndIdempotencyKey(accId, idem)
        .orElseThrow(() -> new Exceptions.Conflict("Idempotency-Key used but transaction record not found"));

  
    TransactionService.TransactionView view = new TransactionService.TransactionView(
        existing.getId(),
        existing.getAccount().getId(),
        existing.getOperationType().id(),
        existing.getAmount(),
        existing.getEventDate()
    );

    if (inserted == 1) {
      return new TransactionService.Result(view, false);
    }

    return new TransactionService.Result(view, true);
  }
}
