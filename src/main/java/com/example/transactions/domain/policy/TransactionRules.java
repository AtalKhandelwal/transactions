package com.example.transactions.domain.policy;

import com.example.transactions.domain.model.OperationType;
import com.example.transactions.support.Exceptions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;


public final class TransactionRules {

  private static final int IDEMPOTENCY_KEY_MAX_LEN = 64;
  private static final Pattern IDEMPOTENCY_KEY_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

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

  public static String validateIdempotencyKey(String idempotencyKey) {
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
      throw new Exceptions.BadRequest("Idempotency-Key header is required");
    }
    if (idempotencyKey.length() > IDEMPOTENCY_KEY_MAX_LEN) {
      throw new Exceptions.BadRequest("Idempotency-Key must be <= " + IDEMPOTENCY_KEY_MAX_LEN + " chars");
    }
    if (!IDEMPOTENCY_KEY_PATTERN.matcher(idempotencyKey).matches()) {
      throw new Exceptions.BadRequest("Idempotency-Key contains invalid characters");
    }
    return idempotencyKey;
  }

  public static BigDecimal toStoredAmount(OperationType operationType, BigDecimal normalizedClientAmount) {
    return operationType.isDebit() ? normalizedClientAmount.negate() : normalizedClientAmount;
  }

  public static String canonicalRequest(long accountId, int operationTypeId, BigDecimal normalizedClientAmount) {
    return "accountId=" + accountId +
        "|op=" + operationTypeId +
        "|amount=" + normalizedClientAmount.toPlainString();
  }
}
