package com.example.transactions.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

  public static final String REQ_ID_HEADER = "X-Request-Id";
  public static final String TRACE_ID_MDC_KEY = "traceId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String traceId = request.getHeader(REQ_ID_HEADER);
    if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();

    MDC.put(TRACE_ID_MDC_KEY, traceId);
    response.setHeader(REQ_ID_HEADER, traceId);

    Instant start = Instant.now();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long ms = Duration.between(start, Instant.now()).toMillis();
      org.slf4j.LoggerFactory.getLogger("access").info(
          "method={} path={} status={} durationMs={}",
          request.getMethod(),
          request.getRequestURI(),
          response.getStatus(),
          ms
      );
      MDC.remove(TRACE_ID_MDC_KEY);
    }
  }
}