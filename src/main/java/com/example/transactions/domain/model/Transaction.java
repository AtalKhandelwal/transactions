package com.example.transactions.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_transactions_idempotency",
        columnNames = {"account_id", "idempotency_key"}))
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Column(name = "operation_type_id", nullable = false)
  private OperationType operationType;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "event_date", nullable = false, insertable = false, updatable = false)
  private Instant eventDate;

  @Column(name = "idempotency_key", nullable = false, length = 64)
  private String idempotencyKey;

  @Column(name = "request_hash", nullable = false, length = 64)
  private String requestHash;

  protected Transaction() { }

  public Transaction(Account account,
                     OperationType operationType,
                     BigDecimal amount,
                     String idempotencyKey,
                     String requestHash) {
    this.account = account;
    this.operationType = operationType;
    this.amount = amount;
    this.idempotencyKey = idempotencyKey;
    this.requestHash = requestHash;
  }


  public Long getId() { return id; }
  public Account getAccount() { return account; }
  public OperationType getOperationType() { return operationType; }
  public BigDecimal getAmount() { return amount; }
  public Instant getEventDate() { return eventDate; }
  public String getIdempotencyKey() { return idempotencyKey; }
  public String getRequestHash() { return requestHash; }
}