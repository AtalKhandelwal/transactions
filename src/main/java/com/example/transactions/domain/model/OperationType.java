package com.example.transactions.domain.model;

import java.util.Arrays;

public enum OperationType {
  CASH_PURCHASE(1, "CASH PURCHASE", true),
  INSTALLMENT_PURCHASE(2, "INSTALLMENT PURCHASE", true),
  WITHDRAWAL(3, "WITHDRAWAL", true),
  PAYMENT(4, "PAYMENT", false);

  private final int id;
  private final String description;
  private final boolean debit; 

  OperationType(int id, String description, boolean debit) {
    this.id = id;
    this.description = description;
    this.debit = debit;
  }

  public int id() { return id; }
  public String description() { return description; }
  public boolean isDebit() { return debit; }

  public static OperationType fromId(int id) {
    return Arrays.stream(values())
        .filter(v -> v.id == id)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown operation_type_id: " + id));
  }
}