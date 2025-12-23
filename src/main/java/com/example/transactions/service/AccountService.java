package com.example.transactions.service;

import com.example.transactions.domain.model.Account;
import com.example.transactions.domain.repository.AccountRepository;
import com.example.transactions.support.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
public class AccountService {

  private static final Logger log = LoggerFactory.getLogger(AccountService.class);

  private final AccountRepository accountRepository;

  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Transactional
  public Account createAccount(String documentNumber) {
    accountRepository.findByDocumentNumber(documentNumber).ifPresent(a -> {
      throw new Exceptions.Conflict("document_number already exists");
    });

    try {
  Account saved = accountRepository.save(new Account(documentNumber));
  log.info("account_created accountId={} documentNumber={}", saved.getId(), saved.getDocumentNumber());
  return saved;
} catch (DataIntegrityViolationException e) {
  throw new Exceptions.Conflict("document_number already exists");
}
  }

  @Transactional(readOnly = true)
  public Account getAccount(long accountId) {
    return accountRepository.findById(accountId)
        .orElseThrow(() -> new Exceptions.NotFound("account not found"));
  }
}