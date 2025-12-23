package com.example.transactions.service.hashing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyHasherConfig {

  @Bean
  public IdempotencyHasher idempotencyHasher() {
    return new IdempotencyHasher();
  }
}