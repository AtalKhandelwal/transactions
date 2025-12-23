package com.example.transactions.api.v1.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
    long transaction_id,
    long account_id,
    int operation_type_id,
    BigDecimal amount,
    Instant event_date
) {}