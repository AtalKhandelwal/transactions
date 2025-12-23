package com.example.transactions.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
    @NotBlank
    @Pattern(regexp = "\\d{11,14}", message = "document_number must be 11 to 14 digits")
    String document_number
) {}