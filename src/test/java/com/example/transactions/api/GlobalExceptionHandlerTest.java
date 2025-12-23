package com.example.transactions.api;

import com.example.transactions.config.RequestCorrelationFilter;
import com.example.transactions.support.Exceptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @AfterEach
  void cleanup() {
    MDC.clear();
  }

  @Test
  void dataIntegrity_is409_withDataIntegrityViolationCode() {
    MDC.put(RequestCorrelationFilter.TRACE_ID_MDC_KEY, "t-1");
    var req = new MockHttpServletRequest("POST", "/v1/transactions");

    ResponseEntity<ApiError> resp =
        handler.handleDataIntegrity(new DataIntegrityViolationException("boom"), req);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    ApiError body = resp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("DATA_INTEGRITY_VIOLATION");
    assertThat(body.path()).isEqualTo("/v1/transactions");
    assertThat(body.trace_id()).isEqualTo("t-1");
    assertThat(body.timestamp()).isNotNull();
  }

  @Test
  void badRequest_is400_withBadRequestCode() {
    MDC.put(RequestCorrelationFilter.TRACE_ID_MDC_KEY, "t-2");
    var req = new MockHttpServletRequest("POST", "/v1/transactions");

    ResponseEntity<ApiError> resp =
        handler.handleBadRequest(new Exceptions.BadRequest("bad input"), req);

    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    ApiError body = resp.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("BAD_REQUEST");
    assertThat(body.message()).isEqualTo("bad input");
    assertThat(body.trace_id()).isEqualTo("t-2");
    assertThat(body.timestamp()).isNotNull();
  }
}
