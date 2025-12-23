# Transactions API

A small, production-friendly REST API to manage accounts and transactions with simple business rules:

- Each cardholder (customer) has an account with their data.
- For each operation carried out by the customer, a transaction is created and associated with this account.
- Each transaction has:
  - a type (cash purchase, installment purchase, withdrawal, or payment)
  - an amount
  - a creation date
- Purchase and withdrawal transactions are recorded with a negative value.
- Payment transactions are recorded with a positive value.


---

## Operation types

| operation_type_id | Type                  | Stored sign |
|------------------:|-----------------------|------------|
| 1                 | CASH_PURCHASE         | negative   |
| 2                 | INSTALLMENT_PURCHASE  | negative   |
| 3                 | WITHDRAWAL            | negative   |
| 4                 | PAYMENT               | positive   |

---

## Quickstart (Docker)

```bash
docker compose up --build
```

API runs on `http://localhost:8080`.

---

## Health check

```bash
curl -s http://localhost:8080/actuator/health
```

---

## Create an account

**POST** `/v1/accounts`

```bash
curl -s -X POST http://localhost:8080/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"document_number":"12345678900"}'
```

---

## Get an account

**GET** `/v1/accounts/{accountId}`

```bash
curl -s http://localhost:8080/v1/accounts/1
```

---

## Create a transaction

**POST** `/v1/transactions`

> `Idempotency-Key` header is required.

```bash
# Purchase / Withdrawal (saved as negative automatically)
curl -s -X POST http://localhost:8080/v1/transactions \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: txn-001' \
  -d '{"account_id":1,"operation_type_id":1,"amount":50.00}'
```

```bash
# Payment (saved as positive)
curl -s -X POST http://localhost:8080/v1/transactions \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: txn-002' \
  -d '{"account_id":1,"operation_type_id":4,"amount":60.00}'
```


- `amount` must be positive
- For `operation_type_id` in `{1,2,3}` (purchases/withdrawal), the API stores the amount as negative automatically
- For `operation_type_id = 4` (payment), the API stores the amount as positive
- `Idempotency-Key` makes transaction creation safe to retry:
  - Same key + same payload ⇒ returns the same transaction (`Idempotent-Replayed: true`)
  - Same key + different payload ⇒ rejected as conflict

---
