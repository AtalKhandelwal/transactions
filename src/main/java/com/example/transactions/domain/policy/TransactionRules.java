package com.example.transactions.domain.policy;

import com.example.transactions.domain.model.OperationType;
import com.example.transactions.support.Exceptions;

import java.math.BigDecimal;
import java.math.RoundingMode;


public final class TransactionRules {

  private TransactionRules() {}

  public static long validateAccountId(Long accountId) {
    if (accountId == null) {
      throw new Exceptions.BadRequest("account_id is required");
    }
    return validateAccountId(accountId.longValue());
  }

  public static long validateAccountId(long accountId) {
    if (accountId <= 0) {
      throw new Exceptions.BadRequest("account_id must be > 0");
    }
    return accountId;
  }

  public static OperationType validateOperationType(Integer operationTypeId) {
    if (operationTypeId == null) {
      throw new Exceptions.BadRequest("operation_type_id is required");
    }
    try {
      return OperationType.fromId(operationTypeId);
    } catch (IllegalArgumentException e) {
      throw new Exceptions.BadRequest(e.getMessage());
    }
  }

  public static BigDecimal normalizeClientAmount(BigDecimal clientAmount) {
    if (clientAmount == null) {
      throw new Exceptions.BadRequest("amount is required");
    }
    if (clientAmount.signum() <= 0) {
      throw new Exceptions.BadRequest("amount must be > 0");
    }
    if (clientAmount.scale() > 2) {
      throw new Exceptions.BadRequest("amount must have at most 2 decimal places");
    }
    return clientAmount.setScale(2, RoundingMode.UNNECESSARY);
  }

  public static BigDecimal toStoredAmount(OperationType operationType, BigDecimal normalizedClientAmount) {
    return operationType.isDebit() ? normalizedClientAmount.negate() : normalizedClientAmount;
  }


}
