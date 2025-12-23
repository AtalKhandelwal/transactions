package com.example.transactions.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "accounts",
    uniqueConstraints = @UniqueConstraint(name = "uk_accounts_document_number", columnNames = "document_number"))
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_number", nullable = false, length = 14)
  private String documentNumber;

  @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
  private Instant createdAt;

  protected Account() { }

  public Account(String documentNumber) {
    this.documentNumber = documentNumber;
  }
  
  public Long getId() { return id; }
  public String getDocumentNumber() { return documentNumber; }
}