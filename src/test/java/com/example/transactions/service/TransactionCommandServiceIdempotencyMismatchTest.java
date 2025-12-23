package com.example.transactions.service;

import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.model.Transaction;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.domain.repository.TransactionRepository;
import com.example.transactions.service.hashing.IdempotencyHasher;
import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionCommandServiceIdempotencyMismatchTest {

  @Test
  void sameIdempotencyKeyDifferentPayload_throwsConflict() {
    AccountRepository accountRepo = mock(AccountRepository.class);
    TransactionRepository txRepo = mock(TransactionRepository.class);

    var acc = new Account("123");

    Account accSpy = spy(acc);
    doReturn(1L).when(accSpy).getId();
    when(accountRepo.findById(1L)).thenReturn(Optional.of(accSpy));

    when(txRepo.insertIfAbsent(anyLong(), anyInt(), any(), anyString(), anyString()))
        .thenReturn(0);

    Transaction existing = mock(Transaction.class);
    when(existing.getId()).thenReturn(10L);
    when(existing.getAmount()).thenReturn(new BigDecimal("10.00"));
    when(existing.getEventDate()).thenReturn(Instant.now());
    when(existing.getRequestHash()).thenReturn("old-hash");

    when(existing.getAccount()).thenReturn(accSpy);
    when(existing.getOperationType()).thenReturn(com.example.transactions.domain.model.OperationType.PAYMENT);

    when(txRepo.findByAccount_IdAndIdempotencyKey(1L, "idem-1")).thenReturn(Optional.of(existing));

    TransactionCommandService svc = new TransactionCommandService(accountRepo, txRepo, new IdempotencyHasher());

    assertThatThrownBy(() ->
        svc.createTransactionTx(1L, 4, new BigDecimal("11.00"), "idem-1")
    ).isInstanceOf(Exceptions.Conflict.class);
  }
}
