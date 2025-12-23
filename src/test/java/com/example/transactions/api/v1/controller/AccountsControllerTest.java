package com.example.transactions.api.v1.controller;

import com.example.transactions.domain.model.Account;
import com.example.transactions.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountsController.class)
class AccountsControllerTest {

  @Autowired MockMvc mvc;
  @MockBean AccountService accountService;

  @Test
  void createAccount_returns201() throws Exception {
    Account acc = new Account("12345678900");
    try {
      var f = Account.class.getDeclaredField("id");
      f.setAccessible(true);
      f.set(acc, 1L);
    } catch (Exception ignored) {}

    when(accountService.createAccount("12345678900")).thenReturn(acc);

    mvc.perform(post("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"document_number\":\"12345678900\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/v1/accounts/1"))
        .andExpect(jsonPath("$.account_id").value(1))
        .andExpect(jsonPath("$.document_number").value("12345678900"));
  }

  @Test
  void createAccount_validation400() throws Exception {
    mvc.perform(post("/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"document_number\":\"abc\"}"))
        .andExpect(status().isBadRequest());
  }
}