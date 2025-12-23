package com.example.transactions.domain.policy;

import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class TransactionRulesTest {

  @Test
  void idempotencyKey_rejectsBlankAndTooLongAndBadChars() {
    assertThatThrownBy(() -> TransactionRules.validateIdempotencyKey(" "))
        .isInstanceOf(Exceptions.BadRequest.class);

    assertThatThrownBy(() -> TransactionRules.validateIdempotencyKey("x".repeat(65)))
        .isInstanceOf(Exceptions.BadRequest.class);

    assertThatThrownBy(() -> TransactionRules.validateIdempotencyKey("bad key"))
        .isInstanceOf(Exceptions.BadRequest.class);
  }

  @Test
  void normalizeAmount_rejectsNullZeroNegativeAndTooManyDecimals() {
    assertThatThrownBy(() -> TransactionRules.normalizeClientAmount(null))
        .isInstanceOf(Exceptions.BadRequest.class);

    assertThatThrownBy(() -> TransactionRules.normalizeClientAmount(new BigDecimal("0.00")))
        .isInstanceOf(Exceptions.BadRequest.class);

    assertThatThrownBy(() -> TransactionRules.normalizeClientAmount(new BigDecimal("-1.00")))
        .isInstanceOf(Exceptions.BadRequest.class);

    assertThatThrownBy(() -> TransactionRules.normalizeClientAmount(new BigDecimal("1.001")))
        .isInstanceOf(Exceptions.BadRequest.class);
  }

  @Test
  void validateAccountId_rejectsNonPositive() {
    assertThatThrownBy(() -> TransactionRules.validateAccountId(0))
        .isInstanceOf(Exceptions.BadRequest.class);
    assertThatThrownBy(() -> TransactionRules.validateAccountId(-1))
        .isInstanceOf(Exceptions.BadRequest.class);
  }
}
