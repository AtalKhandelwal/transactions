package com.example.transactions.api;

import com.example.transactions.config.RequestCorrelationFilter;
import com.example.transactions.support.Exceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestControllerAdvice
public class GlobalExceptionHandler {
private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MissingRequestHeaderException.class)
public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest req) {
  return build(
      HttpStatus.BAD_REQUEST,
      "BAD_REQUEST",
      "Missing required header: " + ex.getHeaderName(),
      req,
      Map.of("header", ex.getHeaderName())
  );
}

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {

    Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors
          .computeIfAbsent(fe.getField(), k -> new ArrayList<>())
          .add(Optional.ofNullable(fe.getDefaultMessage()).orElse("invalid"));
    }

    List<String> globalErrors = ex.getBindingResult().getGlobalErrors().stream()
        .map(err -> Optional.ofNullable(err.getDefaultMessage()).orElse("invalid"))
        .toList();

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("field_errors", fieldErrors);
    if (!globalErrors.isEmpty()) {
      details.put("global_errors", globalErrors);
    }

    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", req, details);
  }
  
  @ExceptionHandler(Exceptions.BadRequest.class)
  public ResponseEntity<ApiError> handleBadRequest(Exceptions.BadRequest ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, Map.of());
  }

  @ExceptionHandler(Exceptions.NotFound.class)
  public ResponseEntity<ApiError> handleNotFound(Exceptions.NotFound ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, Map.of());
  }

  @ExceptionHandler(Exceptions.Conflict.class)
  public ResponseEntity<ApiError> handleConflict(Exceptions.Conflict ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req, Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "Request conflicts with current state", req, Map.of());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), req, Map.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnhandled(Exception ex, HttpServletRequest req) {
    String traceId = MDC.get(RequestCorrelationFilter.TRACE_ID_MDC_KEY);
    log.error("unhandled_exception traceId={} path={}", traceId, req.getRequestURI(), ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error", req, Map.of());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request body", req, Map.of());
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req, Map<String, Object> details) {
    String traceId = MDC.get(RequestCorrelationFilter.TRACE_ID_MDC_KEY);
    ApiError body = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        code,
        message,
        req.getRequestURI(),
        traceId,
        details == null ? Map.of() : details
    );
    return ResponseEntity.status(status).body(body);
  }


}