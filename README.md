# Stampede

A flash-sale order system built to handle high concurrency — modeled after real-world scenarios like 10,000 concurrent users competing for 100 limited items.

Built iteratively across versions, each solving a specific bottleneck. Every version documents what it handles, how it was tested, and why it falls short at scale — motivating the next version.

---

## Architecture Evolution

| Version | Storage | Concurrency mechanism | Servers | Status |
|---|---|---|---|---|
| V1 | In-memory | `AtomicInteger` (CAS) | Single | ✅ Current |
| V2 | PostgreSQL | DB atomic UPDATE + transactions | Single → Multi | Planned |
| V3 | Redis + PostgreSQL | Redis `DECR` as gate, DB for durability | Multi | Planned |

---

## V1 — In-Memory, Single Server

### What it does
- Products seeded into memory at startup
- Stock managed with `AtomicInteger` per product — lock-free, CAS-based decrement
- Orders stored in a `ConcurrentLinkedQueue`
- REST API: list products, get product, place order

### Stack
- **Backend:** Java 25, Spring Boot 3.5, Maven
- **Storage:** JVM heap (no database)

### API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Server health check |
| `GET` | `/api/products` | List all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `POST` | `/api/orders/order` | Place an order |

**Buy request body:**
```json
{ "personId": "user1", "productId": "p1", "quantity": 1 }
```

**Responses:**
- `200 OK` — order placed successfully
- `409 Conflict` — sold out or product not found
- `404 Not Found` — product does not exist

### How to run
```bash
cd backend
mvn spring-boot:run
```
Server starts on `http://localhost:8080`

### Concurrency model

Spring Boot (Tomcat) assigns one thread per request from a pool of ~200 threads. Multiple threads run simultaneously on different CPU cores — true parallelism. Without protection, concurrent buyers reading and decrementing the same stock count would cause overselling.

**Fix:** `AtomicInteger` per product. Each decrement uses Compare-And-Swap (CAS) — a single atomic CPU instruction that reads, checks, and writes only if the value hasn't changed since the read. No thread sleeps or waits; losers retry in nanoseconds.

```
Thread-1: read stock=5 → CAS(5→4) → SUCCESS
Thread-2: read stock=5 → CAS(5→4) → FAIL (it's 4 now) → retry → read 4 → CAS(4→3) → SUCCESS
```

Stock can never go negative. No mutex, no blocking.

### Load capacity (estimated)

| Concurrent users | Throughput | Latency (p99) | Notes |
|---|---|---|---|
| 50 | ~40,000 req/s | < 5ms | Purely CPU/memory bound |
| 200 | ~40,000 req/s | < 10ms | Thread pool fully utilised |
| 500 | ~35,000 req/s | ~20ms | Tomcat queue pressure |

*Load test results will be added after k6 testing.*

### Limitations

1. **No persistence** — restart the server and all stock + order data is lost. Stock resets to initial values even if items were sold.
2. **Single server only** — `AtomicInteger` lives in the JVM heap. Two server instances each have their own copy. A user buying on Server-A and Server-B simultaneously can both succeed when only one item remains — oversell across servers.
3. **No order history queries** — orders are in a queue in memory, not queryable by product or time.
4. **No authentication** — any `personId` string is accepted; no real user validation.

### Why V1 is not production-ready

The fundamental issue: **a flash sale system must survive server restarts and run on multiple instances for reliability**. Both requirements are impossible with in-memory storage alone.

→ These limitations motivate **V2: PostgreSQL with atomic DB transactions**

---

## Project Structure

```
Stampede/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/stampede/
│       ├── StampedeApplication.java     ← entry point
│       ├── model/
│       │   ├── Product.java             ← product data
│       │   ├── Order.java               ← order data
│       │   ├── OrderStatus.java         ← PENDING / COMPLETED / CANCELLED
│       │   ├── BuyRequest.java          ← request body for POST /orders/order
│       │   └── PersonAccount.java       ← user account data
│       ├── service/
│       │   ├── ProductService.java      ← catalog + live stock (AtomicInteger)
│       │   └── OrderService.java        ← buy logic, order storage
│       └── web/
│           ├── HealthController.java    ← GET /api/health
│           ├── ProductController.java   ← GET /api/products/**
│           └── OrderController.java     ← POST /api/orders/order
└── README.md
```
