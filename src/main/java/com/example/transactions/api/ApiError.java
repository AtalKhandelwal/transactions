package com.example.transactions.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String code,
    String message,
    String path,
    String trace_id,
    Map<String, Object> details
) {}