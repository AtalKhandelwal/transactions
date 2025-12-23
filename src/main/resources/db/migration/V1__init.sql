CREATE TABLE IF NOT EXISTS accounts (
  id BIGSERIAL PRIMARY KEY,
  document_number VARCHAR(14) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT uk_accounts_document_number UNIQUE (document_number)
);

CREATE TABLE IF NOT EXISTS transactions (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  operation_type_id INT NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  event_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  idempotency_key VARCHAR(64) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,

  CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id),
  CONSTRAINT uk_transactions_idempotency UNIQUE (account_id, idempotency_key),
  CONSTRAINT chk_transactions_amount_sign
  CHECK ((operation_type_id IN (1,2,3) AND amount < 0) OR (operation_type_id = 4 AND amount > 0))
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
