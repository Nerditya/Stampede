# Stampede

Stampede is a flash-sale ordering system built to explore both correctness and capacity as the architecture evolves. The current repo implements the V2 architecture: PostgreSQL-backed inventory, server-side JWT auth, and transactional stock updates that prevent overselling across application instances.

## Current status

| Version | Storage | Concurrency control | Status |
|---|---|---|---|
| V1 | JVM memory | `AtomicInteger` compare-and-swap | Historical reference only |
| V2 | PostgreSQL 16 | Conditional stock update inside a transaction | Implemented and validated locally |
| V3 | Redis + PostgreSQL | Redis admission gate + durable DB writes | Planned for later |

## V2 architecture

```text
Browser / client
  ↓
React frontend (Vite + static assets)
  ↓
Spring Boot backend
  ↓
PostgreSQL 16
```

### Components

- Frontend: React + Vite, served locally via Vite and deployed as a Cloud Run frontend.
- Backend: Java 21 + Spring Boot 3.5 + Spring Security + JPA/Hibernate.
- Database: PostgreSQL 16 for products, orders, refresh tokens, and person records.
- Auth: email/password registration, email verification workflow, JWT access tokens, and rotating refresh tokens stored in PostgreSQL.
- Deployment: Cloud Run for the app and a local PostgreSQL container for development and benchmarking.
- Secrets: database credentials, JWT signing key, and Gmail SMTP credentials are expected from environment variables or secret managers.

### Live services

- Frontend: https://stampede-frontend-6jatj5ld7a-el.a.run.app
- Backend health: https://stampede-backend-6jatj5ld7a-el.a.run.app/api/health

## V2 product and order flow

The app supports a simple but realistic flash-sale flow:

1. A customer registers with name, email, and password.
2. The backend sends a verification email with a one-time token.
3. After verification, the customer logs in and receives an access token and refresh token.
4. The client sends the bearer access token on order requests.
5. The server atomically decreases stock with a conditional SQL update in a transaction.
6. If stock is insufficient, the order is rejected with a clean business error instead of overselling inventory.

### API summary

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| `GET` | `/api/health` | Public | Health check |
| `GET` | `/api/products` | Public | List products and live stock |
| `GET` | `/api/products/{productId}` | Public | Fetch a product |
| `POST` | `/api/products/{productId}/restock` | Admin bearer token | Add units to a product's live stock |
| `POST` | `/api/auth/register` | Public | Register a new account and send verification email |
| `GET` | `/api/auth/verify?token=...` | Public | Verify an email token |
| `POST` | `/api/auth/login` | Public | Login and receive access/refresh tokens |
| `POST` | `/api/auth/refresh` | Public | Rotate a refresh token |
| `POST` | `/api/auth/logout` | Public | Revoke a refresh token |
| `POST` | `/api/orders/order` | Bearer token | Place an order |
| `GET` | `/api/orders/my` | Bearer token | Fetch the caller's orders |

Example checkout payload:

```json
{
  "items": [
    { "productId": "p1", "quantity": 1 }
  ]
}
```

The caller identity is taken from the JWT, not from the request body. In V2, the database enforces stock safety with a conditional update such as `UPDATE ... WHERE live_stock >= quantity`, not with a pessimistic database lock and not with a version-based optimistic lock. This is an atomic, transaction-scoped check-and-decrement pattern: the row is updated only when enough stock exists, and if any line item cannot be fulfilled the transaction rolls back and the order is rejected.

Inventory can be replenished by an administrator with `POST /api/products/{productId}/restock` and a JSON body such as `{"quantity": 1000}`. This atomically adds to `liveStock`; it does not change `initialStock`. The endpoint requires a JWT whose role is `ADMIN`.

This is a standard flash-sale pattern for inventory counters: each product row is decremented by a single atomic SQL statement, rather than reading the value in Java, locking it, and then writing it back. PostgreSQL serializes the competing updates safely, so the update either succeeds or it does not affect any row when stock is exhausted.

## Authentication and security

The backend uses stateless JWT authentication with stateless session management and server-side refresh-token storage.

- Access tokens are short-lived.
- Refresh tokens are opaque and stored in PostgreSQL.
- Refresh rotation prevents replay attacks.
- Protected endpoints reject unauthenticated requests with `401`.
- CORS is configured for the deployed frontend and local Vite frontend.
- Passwords are encoded with BCrypt.

## Local development

### Prerequisites

- Docker
- Java 21+
- Maven
- k6 for load tests

### Start local PostgreSQL

Use the instructions in [docker-instructions.md](docker-instructions.md) to start the local `stampede-db` container.

### Run the backend

```bash
cd backend
mvn spring-boot:run
```

The app listens on `http://localhost:8080`.

### Run the frontend locally

```bash
cd frontend
npm install
npm run dev
```

The Vite frontend runs on `http://localhost:5173` and proxies `/api` to the backend.

### Run backend tests

```bash
cd backend
mvn test
```

## Load testing and concurrency validation

The repo includes a local V2 benchmark script: `load-tests/v2-authenticated.js`.

### V2 benchmark result (verified locally)

Test conditions:

- Local backend: `http://localhost:8080`
- Local PostgreSQL: `stampede-db`
- Product stock: `p1` set to 1,000 units, plus multiple additional products seeded for realistic inventory
- Auth: 5 verified local users, password `password`
- Load: 50 VUs for 15 seconds
- Behavior: each VU logs in and attempts a purchase for `p1` until stock is exhausted

Verified output from the run:

```text
checks_total.......: 25860
checks_succeeded...: 100.00% 25860 out of 25860
purchased...........: 1000 64.57/s
sold_out...........: 24855 1604.93/s
http_reqs...........: 25860 1669.82/s
avg latency.........: 28.87ms
p(90)...............: 45.89ms
p(95)...............: 66.23ms
max latency.........: 367.62ms
```

### Interpretation

This was a controlled sold-out benchmark. The first 1,000 requests succeeded, and the remaining attempts were intentionally rejected with `409` because the product had reached zero stock. That is the correct behavior for a flash-sale system under V2 rules.

The database confirms the system stayed correct:

```text
p1 | initial_stock = 1000 | live_stock = 0
p2 | initial_stock = 500 | live_stock = 500
p3 | initial_stock = 250 | live_stock = 250
p4 | initial_stock = 1000 | live_stock = 1000
p5 | initial_stock = 750 | live_stock = 750
```

There were no negative stock values and the total successful orders were exactly 1,000.

> The k6 `http_req_failed` threshold is expected to cross when the product is sold out because `409` responses are valid business-level rejections. The key correctness metric is that no oversell occurred.

## V1 historical benchmarks

These results are retained for comparison only and describe the older in-memory V1 implementation.

### 200 VUs, stress test

```text
Duration: 10s | Total requests: 62,381 | Throughput: ~6,200 req/s
Purchased: 100 | Sold out (409): 62,205 | Connection errors: 76 (0.12%)
Average latency: 23ms | p(95): 62ms
```

### 200 VUs, realistic think time

```text
Duration: 30s | Total requests: 6,030 | Throughput: ~192 req/s
Purchased: 100 | Sold out (409): 5,833 | Connection errors: 97 (1.6%)
Average latency: 8.66ms | p(95): 2.58ms
```

### 1,000 VUs, Linux stress test

```text
Duration: 10s | Total requests: 250,347 | Throughput: ~24,990 req/s
Purchased: 100 | Sold out (409): 250,247 | HTTP failures: 0%
Average latency: 30.64ms | p(95): 75.39ms
```

The V1 numbers are historical only. They do not represent the current V2 PostgreSQL architecture.

## V3 roadmap (deferred)

V3 is intentionally kept for later work. The current repo is not a Redis-first implementation.

Planned V3 direction:

1. Add Redis-backed admission control before the DB write path.
2. Keep PostgreSQL as the source of truth for durable order state.
3. Separate burst protection from transactional correctness.
4. Measure whether the DB or the admission layer is the true bottleneck.

## Project structure

```text
backend/
  src/main/java/com/stampede/
  src/test/java/com/stampede/
frontend/
load-tests/
README.md
docker-instructions.md
```

## Summary

Stampede is currently a V2 flash-sale system with correct PostgreSQL inventory semantics, a working JWT auth flow, and a validated local benchmark showing the system can safely sell the available stock while rejecting oversubscribed requests cleanly. V3 is architecturally planned but intentionally deferred until the current V2 baseline is fully understood and measured.
