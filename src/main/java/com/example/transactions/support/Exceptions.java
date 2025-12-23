package com.example.transactions.support;

public final class Exceptions {

  private Exceptions() {}

  public static class NotFound extends RuntimeException {
    public NotFound(String message) { super(message); }
  }

  public static class Conflict extends RuntimeException {
    public Conflict(String message) { super(message); }
  }

  public static class BadRequest extends RuntimeException {
    public BadRequest(String message) { super(message); }
  }
}