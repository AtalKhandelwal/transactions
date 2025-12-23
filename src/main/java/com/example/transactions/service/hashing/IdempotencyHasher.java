package com.example.transactions.service.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class IdempotencyHasher {

  private static final HexFormat HEX = HexFormat.of();

  public String sha256(String canonical) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
      return HEX.formatHex(hash);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compute hash", e);
    }
  }
}