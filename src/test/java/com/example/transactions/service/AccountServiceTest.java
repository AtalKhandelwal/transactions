package com.example.transactions.service;

import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.hibernate.exception.ConstraintViolationException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountServiceTest {

  @Test
  void createAccount_conflictOnDuplicateDocument() {
    AccountRepository repo = mock(AccountRepository.class);
    ConstraintViolationException cve = new ConstraintViolationException(
        "duplicate",
        null,
        "uk_accounts_document_number"
    );
    when(repo.save(any(Account.class))).thenThrow(new DataIntegrityViolationException("duplicate", cve));

    AccountService svc = new AccountService(repo);

    assertThatThrownBy(() -> svc.createAccount("123"))
        .isInstanceOf(Exceptions.Conflict.class)
        .hasMessageContaining("document_number already exists");
  }

  @Test
  void getAccount_notFound() {
    AccountRepository repo = mock(AccountRepository.class);
    when(repo.findById(1L)).thenReturn(Optional.empty());
    AccountService svc = new AccountService(repo);

    assertThatThrownBy(() -> svc.getAccount(1L))
        .isInstanceOf(Exceptions.NotFound.class);
  }

  @Test
void createAccount_conflictOnDuplicateDocument_raceCondition() {
  AccountRepository repo = mock(AccountRepository.class);
  when(repo.findByDocumentNumber("123")).thenReturn(Optional.empty());
  when(repo.save(any(Account.class))).thenThrow(new DataIntegrityViolationException("uk_accounts_document_number"));
  AccountService svc = new AccountService(repo);

  assertThatThrownBy(() -> svc.createAccount("123"))
      .isInstanceOf(Exceptions.Conflict.class)
      .hasMessageContaining("document_number already exists");
}

}