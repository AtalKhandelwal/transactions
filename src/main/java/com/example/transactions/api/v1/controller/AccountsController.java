package com.example.transactions.api.v1.controller;

import com.example.transactions.api.v1.dto.AccountResponse;
import com.example.transactions.api.v1.dto.CreateAccountRequest;
import com.example.transactions.domain.model.Account;
import com.example.transactions.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/accounts")
@Validated
public class AccountsController {

  private final AccountService accountService;

  public AccountsController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping
  public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
    Account created = accountService.createAccount(req.document_number());
    AccountResponse body = new AccountResponse(created.getId(), created.getDocumentNumber());
    return ResponseEntity
        .created(URI.create("/v1/accounts/" + created.getId()))
        .body(body);
  }

  @GetMapping("/{accountId}")
  public ResponseEntity<AccountResponse> get(@PathVariable @Positive long accountId) {
    Account acc = accountService.getAccount(accountId);
    return ResponseEntity.ok(new AccountResponse(acc.getId(), acc.getDocumentNumber()));
  }
}