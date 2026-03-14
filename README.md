# FulfillFlow

> An event-driven order-fulfilment and delivery-management platform built as a
> portfolio project. All products, users, orders, and operational data are
> **100% synthetic** -- no real employer data, credentials, or business rules are
> represented anywhere in this repository.

FulfillFlow demonstrates a production-style, distributed backend: customers place
orders, inventory is reserved, deliveries are scheduled, and notifications are
generated -- all coordinated through choreographed Kafka events with reliable
outbox publication, idempotent consumption, saga compensation, and full
observability.

---

## 1. Project purpose

FulfillFlow exists to demonstrate end-to-end engineering of a reliable
event-driven system: domain modelling, asynchronous orchestration, failure
compensation, and operational tooling. It is a learning and portfolio artifact,
not a commercial product.

## 2. Portfolio context

This is a personal portfolio project. It is designed to evidence senior-level
full-stack and distributed-systems skills:

- Java/Spring Boot microservices with Spring for Apache Kafka
- React/TypeScript frontends with accessible, role-aware UI
- Reliable messaging (transactional outbox, idempotent consumers, dead-letter)
- Choreography-based sagas with compensation
- PostgreSQL per-service data ownership with Flyway migrations
- OAuth2/OIDC auth via Keycloak
- Testcontainers-based integration testing
- Docker Compose local run + Kubernetes manifests
- Observability (logs, metrics, traces)

## 3. Main features

- **Customer flow:** browse catalogue, cart, checkout, order creation, order
  timeline, live status, notifications, cancellation.
- **Order lifecycle:** PENDING_INVENTORY -> INVENTORY_RESERVED -> DELIVERY_PENDING
  -> CONFIRMED -> PICKED_UP -> IN_TRANSIT -> DELIVERED, plus cancellation paths.
- **Inventory:** atomic multi-product reservations, no oversell, release on
  compensation, stock adjustments.
- **Delivery:** courier assignment, state-machine transitions, failure handling.
- **Notification:** simulated providers (no real email/SMS), inspectable records.
- **Resilience:** transactional outbox, idempotent consumers, retry + dead-letter,
  controlled replay, compensation.
- **Operations:** admin dashboards, failed-workflow inspection, dead-letter
  inspection, authorized replay.
- **Observability:** structured logging, metrics, distributed tracing.

## 4. Technology stack

> Selected mutually compatible **stable** versions. See
> [`docs/architecture/versions.md`](docs/architecture/versions.md) for the full
> selection rationale.

| Layer | Technology | Version |
|---|---|---|
| Backend language | Java | 21 (LTS) |
| Backend framework | Spring Boot | 3.3.x |
| Messaging | Apache Kafka (KRaft mode) | 3.7.x |
| Persistence | PostgreSQL | 16 |
| Migrations | Flyway | (managed by Spring Boot BOM) |
| Identity | Keycloak (OAuth2/OIDC) | 25.x |
| Frontend language | TypeScript (strict) | 5.4.x |
| Frontend framework | React | 18.x |
| Frontend build | Vite | 5.x |
| Data fetching | TanStack Query | 5.x |
| Forms | React Hook Form | 7.x |
| Validation | Zod | 3.x |
| Frontend testing | Vitest + Testing Library | latest stable |
| E2E testing | Playwright | latest stable |
| Backend testing | JUnit 5, AssertJ, Testcontainers, Awaitility, REST Assured | latest stable |
| Containerization | Docker / Docker Compose | Compose v2 |
| Orchestration | Kubernetes (manifests) | - |
| Metrics | Prometheus | latest stable |
| Dashboards | Grafana | latest stable |
| Tracing | OpenTelemetry Collector + Jaeger/Tempo | latest stable |
| CI/CD | GitHub Actions | - |

## 5. Architecture diagram

```
                         +-------------+
                         |   Frontend  |  React + TypeScript (Vite)
                         +------+------+
                                | REST (OAuth2 Access Code + PKCE)
                                v
        +-----------+----------+----------+-----------+
        v           v          v          v           v
   +---------+ +---------+ +---------+ +-------------+
   |  Order  | |Inventory| |Delivery | |Notification |   Spring Boot
   | Service | | Service | | Service | |   Service   |   (resource servers)
   +----+----+ +----+----+ +----+----+ +------+------+
        |          |          |             |
        |  Each service owns its own PostgreSQL database |
        +-------+----------+----+------+------+
                v          v           v
            +---------------------------------+
            |       Apache Kafka (KRaft)     |  choreographed events
            +---------------------------------+
                         ^
        +----------------+-----------------+
        |   Keycloak (OAuth2 / OIDC)       |
        +---------------------------------+
```

Detailed sequence diagrams (successful order + compensation) are added in later
milestones under [`docs/architecture/`](docs/architecture/).

## 6. Successful order sequence diagram

> Added in the event contracts milestone. See
> [`docs/architecture/sequence-success.md`](docs/architecture/sequence-success.md)
> (placeholder during Milestone 0).

## 7. Compensation sequence diagram

> Added in the compensation milestone. See
> [`docs/architecture/sequence-compensation.md`](docs/architecture/sequence-compensation.md)
> (placeholder during Milestone 0).

## 8. Service and port table

| Component | Port(s) | Notes |
|---|---|---|
| Order Service | 8081 | REST API + actuator |
| Inventory Service | 8082 | REST API + actuator |
| Delivery Service | 8083 | REST API + actuator |
| Notification Service | 8084 | REST API + actuator |
| Frontend (Vite dev) | 5173 | React dev server |
| PostgreSQL (host) | 5432 | distinct DBs per service |
| Kafka brokers | 9092 (internal), 29092 (host) | KRaft mode |
| Keycloak | 8080 | realm `fulfillflow` |
| Prometheus | 9095 | monitoring profile |
| Grafana | 3000 | monitoring profile |
| Jaeger UI | 16686 | monitoring profile |

> Exact host-port mappings are finalized in the infrastructure milestone and
> reflected in [`.env.example`](.env.example) and `compose.yaml`.

## 9. Event catalogue

> Documented in [`contracts/event-schemas/`](contracts/event-schemas/) and
> [`docs/api/events.md`](docs/api/events.md) in the event contracts milestone.
> Includes the event envelope and all domain events (OrderCreated,
> InventoryReserved, DeliveryScheduled, etc.).

## 10. Security model

- OAuth2 / OpenID Connect via Keycloak.
- React uses Authorization Code flow with PKCE.
- Backend services validate JWT access tokens (Spring OAuth2 Resource Server).
- Role-level and ownership-level authorization enforced server-side; identity is
  derived from validated token claims, never from request bodies.
- Narrow CORS, security headers, no secrets in the repo. Development-only
  credentials are documented in `.env.example`.

See [`docs/architecture/security.md`](docs/architecture/security.md) (added in the
identity milestone) and [`SECURITY.md`](SECURITY.md).

## 11. Local setup

FulfillFlow ships in two layers: the **infrastructure** (PostgreSQL, Kafka,
Keycloak) runs in Docker, and the **application layer** (the four Spring Boot
services + the React frontend) runs on your host so you get hot reload and easy
debugging. Everything below assumes you are at the repository root.

> TL;DR for veterans: `make setup && make start-infra`, then
> `mvn -q -T 1C spring-boot:run -pl services/order-service` (repeat per
> service), then `cd frontend && npm install && npm run dev`, open
> <http://localhost:5173>.

### 11.1 Prerequisites

| Tool           | Version      | Check with        |
|----------------|--------------|-------------------|
| Docker Engine  | 24+          | `docker --version`|
| Docker Compose | v2 (plugin)  | `docker compose version` |
| Java JDK       | 21           | `java -version`   |
| Apache Maven   | 3.9+         | `mvn -v`          |
| Node.js        | 20 LTS+      | `node -v`         |
| npm            | 10+          | `npm -v`          |
| GNU Make       | any          | `make -v`         |

Make is optional — every `make` target below has the raw `docker compose`
equivalent next to it, so you can run the project without it.

### 11.2 Step 1 — Get the code and create your `.env`

```bash
git clone https://github.com/omar-sanad/FulfillFlow.git
cd FulfillFlow
make setup            # copies .env.example -> .env (safe to re-run)
```

If you do not have `make`, do it manually:

```bash
cp .env.example .env
```

The defaults in `.env` are development-only synthetic values, so no edits are
required for a first run. Open `.env` only if you want to change ports or
passwords.

### 11.3 Step 2 — Start the infrastructure in Docker

This brings up PostgreSQL (one logical DB per service), Kafka in KRaft mode
(no ZooKeeper), topic provisioning, and Keycloak with the pre-built
`fulfillflow` realm (roles + demo users) imported automatically.

```bash
make start-infra
# equivalent:
# docker compose --env-file .env -f compose.yaml up -d
```

Wait until everything is healthy (Keycloak takes ~30 s on first boot):

```bash
docker compose --env-file .env -f compose.yaml ps
# all services should show status "healthy"
```

What is now running:

| Container               | Host port | Purpose                                  |
|-------------------------|-----------|------------------------------------------|
| `fulfillflow-postgres`  | 5432      | 4 databases: order/inventory/delivery/notification |
| `fulfillflow-kafka`     | 29092     | Kafka broker (KRaft)                     |
| `fulfillflow-kafka-init`| —         | One-shot topic provisioning (exits 0)    |
| `fulfillflow-keycloak`  | 8080      | OAuth2 / OIDC identity provider          |

### 11.4 Step 3 — Build and run the backend services

The backend is a multi-module Maven project (`common` + 4 services). Build it
once so the shared `common` library installs into your local Maven cache:

```bash
mvn -q -T 1C clean install -DskipTests
```

Then start the four services. Open **four separate terminals** (or background
each one) and run one per service:

```bash
# Terminal 1
mvn -q spring-boot:run -pl services/inventory-service

# Terminal 2
mvn -q spring-boot:run -pl services/order-service

# Terminal 3
mvn -q spring-boot:run -pl services/delivery-service

# Terminal 4
mvn -q spring-boot:run -pl services/notification-service
```

Each service runs Flyway migrations on startup, so its database schema is
created automatically. Verify they are up:

```bash
curl -s http://localhost:8081/actuator/health   # order
curl -s http://localhost:8082/actuator/health   # inventory
curl -s http://localhost:8083/actuator/health   # delivery
curl -s http://localhost:8084/actuator/health   # notification
# each should return {"status":"UP"}
```

| Service                | Port | API base             | Swagger UI                                |
|------------------------|------|----------------------|-------------------------------------------|
| order-service          | 8081 | `/api/orders`        | http://localhost:8081/swagger-ui.html     |
| inventory-service      | 8082 | `/api/products`      | http://localhost:8082/swagger-ui.html     |
| delivery-service       | 8083 | `/api/deliveries`    | http://localhost:8083/swagger-ui.html     |
| notification-service   | 8084 | `/api/notifications` | http://localhost:8084/swagger-ui.html     |

### 11.5 Step 4 — Run the frontend

The frontend is a Vite dev server that proxies `/api/*` to the four backend
ports and `/realms/*` to Keycloak (see `frontend/vite.config.ts`).

```bash
cd frontend
npm install
npm run dev      # serves http://localhost:5173
```

Open <http://localhost:5173>. The Keycloak realm already whitelists
`http://localhost:5173` as a valid origin, so login works out of the box.

### 11.6 Step 5 — Log in and try the saga

Use any **demonstration user** from section 13 (e.g. `customer` /
`customer-dev`). The login page has one-click demo-fill buttons for the
customer and admin accounts.

Once logged in as an **administrator** you can seed the catalog, then walk the
full order saga:

```bash
# 1. Get an access token (administrator)
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/fulfillflow/protocol/openid-connect/token" \
  -d "grant_type=password" -d "client_id=fulfillflow-frontend" \
  -d "username=administrator" -d "password=admin-dev" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

# 2. Create a product (administrator or warehouse role)
curl -s -X POST http://localhost:8082/api/products \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"sku":"WIDGET-01","name":"Saga Widget","description":"Demo product","priceCents":5000,"currency":"USD","weightGrams":250}'

# 3. Restock it (grab the product id from the previous response)
curl -s -X POST http://localhost:8082/api/products/<PRODUCT_ID>/restock \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"quantity":100}'
```

Now log in to the UI as **customer** (`customer` / `customer-dev`) and:

1. **Catalog** → add the product to an order.
2. The order is created (`order.created` event), inventory reserves stock.
3. **Orders** → open the order → **Pay**. The saga auto-schedules a delivery
   (`delivery.scheduled`).
4. **Deliveries** → **Pickup** (→ `IN_TRANSIT`) → **Complete**
   (→ `delivery.completed`). The order auto-fulfils (`order.fulfilled`) and
   inventory confirms the reservation.
5. **Notifications** shows every event-driven email/SMS that was emitted.

The compensation path: after paying, use **Fail** on the delivery instead of
**Complete** — the order transitions to `FAILED` and the stock reservation is
released.

### 11.7 Step 6 — Stop and clean up

```bash
make stop          # stops containers, keeps data volumes
# equivalent:
# docker compose --env-file .env -f compose.yaml down

# Stop the four Java services with Ctrl+C in each terminal.
# Stop the frontend with Ctrl+C.
```

To wipe all persistent data (databases, Kafka, Keycloak) and start fresh:

```bash
docker compose --env-file .env -f compose.yaml down -v
```

### 11.8 Frontend only (production build)

```bash
cd frontend
npm install
npm run build    # type-check + production build to dist/
npm run preview  # serve the production build on :5173
```

The proxy rewrites `/api/order → :8081`, `/api/inventory → :8082`,
`/api/delivery → :8083`, and `/api/notification → :8084`. Keycloak runs on
`:8080`.

### 11.9 Troubleshooting

- **`port is already allocated`** — another process is using a port listed in
  `.env`. Either stop that process or change the `*_PORT` variable in `.env`.
- **Services fail to start with a Keycloak/JWT error** — Keycloak was not yet
  healthy. Run `docker compose ps` and wait until `fulfillflow-keycloak`
  shows `healthy`, then restart the Java services.
- **`No products available` in the UI** — the catalog starts empty. Log in as
  administrator/warehouse and create + restock a product (Step 5).
- **Login button does nothing / 401** — the access token expires (~5 min).
  Log out and back in, or refresh the page.
- **Flyway `validate` errors after a schema change** — run
  `docker compose down -v` to reset the databases, then `make start-infra`
  and restart the services.

## 12. Test instructions

> Backend unit + Testcontainers integration tests, Vitest component tests, and
> Playwright E2E tests are added progressively per milestone. See
> [`docs/testing/`](docs/testing/) for the evolving strategy.

## 13. Demonstration users

Seeded synthetic Keycloak users (created by `infra/keycloak/fulfillflow-realm.json`):

| Role      | Username  | Password        |
|-----------|-----------|-----------------|
| customer  | customer  | customer-dev    |
| admin     | admin     | admin-dev       |
| warehouse | warehouse | warehouse-dev   |
| courier   | courier   | courier-dev      |

The login screen exposes one-click demo-fill buttons for the customer and
admin accounts.

## 14. Screenshots

> Added during the React and operations milestones.

## 15. Observability instructions

> Structured JSON logging, Prometheus metrics, and OpenTelemetry tracing are
> wired up in the observability milestone. Dashboards and trace-viewing
> instructions will be documented under
> [`docs/architecture/observability.md`](docs/architecture/observability.md).

## 16. Known limitations

- No real payment processing (simulated or future extension).
- Local demonstration manifests are not production-hardened; managed PostgreSQL
  and Kafka should be used in production.
- Realtime status updates may begin as polling and migrate to SSE/WebSocket.

## 17. Roadmap

See the milestone list in the project specification; the roadmap tracks the
fifteen milestones from repository foundation to portfolio release.

## 18. License

Released under the MIT License. See [`LICENSE`](LICENSE).

## 19. Synthetic data statement

**All data in FulfillFlow is synthetic.** Product names, users, orders,
inventory, couriers, and operational records are generated for demonstration
only. No real person, employer, customer, or proprietary business rule is
represented.

## 20. Hardest engineering problems solved

> This section is updated as milestones land. Anticipated hard problems:

- **Reliable event publication without dual-writes:** the transactional outbox
  pattern keeps domain state and outbox events atomic within one DB transaction.
- **Idempotent consumption under at-least-once delivery:** inbox tables with a
  unique `eventId` constraint make duplicate Kafka delivery a no-op.
- **Concurrency without oversell:** explicit concurrency control for multi-product
  atomic reservations in the inventory service.
- **Saga compensation:** choreography-based failure handling that releases
  reservations and cancels deliveries while keeping order state consistent.

---

## Status

**Milestone 0 -- Repository foundation** is complete. Later milestones build the
infrastructure, services, frontend, and observability layers incrementally.
