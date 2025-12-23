package com.example.transactions.api.v1.controller;

import com.example.transactions.api.v1.dto.CreateTransactionRequest;
import com.example.transactions.api.v1.dto.TransactionResponse;
import com.example.transactions.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionsController {

  private final TransactionService transactionService;

  public TransactionsController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping
  public ResponseEntity<TransactionResponse> create(
      @RequestHeader(name = "Idempotency-Key", required = true) String idempotencyKey,
      @Valid @RequestBody CreateTransactionRequest req) {

    TransactionService.Result result = transactionService.createTransaction(
        req.account_id(),
        req.operation_type_id(),
        req.amount(),
        idempotencyKey
    );

    TransactionService.TransactionView tx = result.transaction();
    TransactionResponse body = new TransactionResponse(
      tx.transactionId(),
      tx.accountId(),
      tx.operationTypeId(),
      tx.amount(),
      tx.eventDate()
    );

    return ResponseEntity.status(201).body(body);
  }
}