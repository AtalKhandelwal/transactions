package com.example.transactions.api.v1.dto;

public record AccountResponse(
    long account_id,
    String document_number
) {}