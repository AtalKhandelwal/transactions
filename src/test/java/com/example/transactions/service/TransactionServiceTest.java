package com.example.transactions.service;

import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.model.OperationType;
import com.example.transactions.domain.model.Transaction;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.domain.repository.TransactionRepository;
import com.example.transactions.service.hashing.IdempotencyHasher;
import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

  private AccountRepository accountRepository;
  private TransactionRepository transactionRepository;
  private TransactionService service;

  @BeforeEach
  void setUp() {
    accountRepository = mock(AccountRepository.class);
    transactionRepository = mock(TransactionRepository.class);
      TransactionCommandService commandService = new TransactionCommandService(
        accountRepository,
        transactionRepository,
        new IdempotencyHasher()
    );
    service = new TransactionService(commandService);
  }

  static Stream<TestRow> signRows() {
    return Stream.of(
        new TestRow(OperationType.CASH_PURCHASE.id(), new BigDecimal("10.00"), new BigDecimal("-10.00")),
        new TestRow(OperationType.INSTALLMENT_PURCHASE.id(), new BigDecimal("10.00"), new BigDecimal("-10.00")),
        new TestRow(OperationType.WITHDRAWAL.id(), new BigDecimal("10.00"), new BigDecimal("-10.00")),
        new TestRow(OperationType.PAYMENT.id(), new BigDecimal("10.00"), new BigDecimal("10.00"))
    );
  }

  record TestRow(int opId, BigDecimal input, BigDecimal expectedStored) {}

  @ParameterizedTest
@MethodSource("signRows")
@DisplayName("Applies correct sign based on operation type (table-driven)")
void appliesSign(TestRow row) {
  Account acc = new Account("12345678900");
  try {
    var idField = Account.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(acc, 1L);
  } catch (Exception ignored) {}

  when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

  when(transactionRepository.insertIfAbsent(eq(1L), eq(row.opId()), any(BigDecimal.class), eq("idem-1"), anyString()))
      .thenReturn(1);


  ArgumentCaptor<BigDecimal> storedAmountCaptor = ArgumentCaptor.forClass(BigDecimal.class);


  String requestHash = new IdempotencyHasher().sha256("accountId=1|op=" + row.opId() + "|amount=10.00");
  Transaction dbRow = new Transaction(acc, OperationType.fromId(row.opId()), row.expectedStored(), "idem-1", requestHash);
  try {
    var f = Transaction.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(dbRow, 99L);
  } catch (Exception ignored) {}

  when(transactionRepository.findByAccount_IdAndIdempotencyKey(1L, "idem-1"))
      .thenReturn(Optional.of(dbRow));

  TransactionService.Result res = service.createTransaction(1L, row.opId(), row.input(), "idem-1");

  verify(transactionRepository).insertIfAbsent(eq(1L), eq(row.opId()), storedAmountCaptor.capture(), eq("idem-1"), anyString());
  assertThat(storedAmountCaptor.getValue()).isEqualByComparingTo(row.expectedStored());
  assertThat(res.replayed()).isFalse();
}

  @Test
@DisplayName("Idempotency: same key + same payload returns existing transaction")
void idempotencyReplay() {
  Account acc = new Account("12345678900");
  try {
    var idField = Account.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(acc, 1L);
  } catch (Exception ignored) {}

  when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));


  when(transactionRepository.insertIfAbsent(eq(1L), eq(4), any(BigDecimal.class), eq("idem-1"), anyString()))
      .thenReturn(0);

  String hash = new IdempotencyHasher().sha256("accountId=1|op=4|amount=10.00");
  Transaction existing = new Transaction(acc, OperationType.PAYMENT, new BigDecimal("10.00"), "idem-1", hash);
  try {
    var f = Transaction.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(existing, 777L);
  } catch (Exception ignored) {}

  when(transactionRepository.findByAccount_IdAndIdempotencyKey(1L, "idem-1"))
      .thenReturn(Optional.of(existing));

  TransactionService.Result res = service.createTransaction(1L, 4, new BigDecimal("10.00"), "idem-1");

  assertThat(res.replayed()).isTrue();
  assertThat(res.transaction().transactionId()).isEqualTo(777L);
}

  @Test
@DisplayName("Idempotency: same key + different payload => 409 conflict")
void idempotencyConflict() {
  Account acc = new Account("12345678900");
  try {
    var idField = Account.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(acc, 1L);
  } catch (Exception ignored) {}

  when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));


  when(transactionRepository.insertIfAbsent(eq(1L), eq(4), any(BigDecimal.class), eq("idem-1"), anyString()))
      .thenReturn(0);


  String existingHash = new IdempotencyHasher().sha256("accountId=1|op=4|amount=10.00");
  Transaction existing = new Transaction(acc, OperationType.PAYMENT, new BigDecimal("10.00"), "idem-1", existingHash);

  when(transactionRepository.findByAccount_IdAndIdempotencyKey(1L, "idem-1"))
      .thenReturn(Optional.of(existing));

  assertThatThrownBy(() -> service.createTransaction(1L, 4, new BigDecimal("99.00"), "idem-1"))
      .isInstanceOf(Exceptions.Conflict.class);
}
}