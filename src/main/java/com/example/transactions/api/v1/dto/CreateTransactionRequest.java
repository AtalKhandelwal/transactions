package com.example.transactions.api.v1.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateTransactionRequest(
    @NotNull @Positive Long account_id,

    @NotNull @Min(1) @Max(4) Integer operation_type_id,

    @NotNull @Positive
    @Digits(integer = 17, fraction = 2, message = "amount must have up to 2 decimal places")
    BigDecimal amount
) {}