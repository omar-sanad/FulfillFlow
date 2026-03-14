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

```bash
make setup   # one-time preparation (see Makefile)
make start   # start Kafka, PostgreSQL, Keycloak, services, frontend
make test    # run the test suite
make stop    # stop everything
make clean   # remove generated artifacts (warns before deleting data)
```

> `make start` will start Kafka, PostgreSQL databases, Keycloak, backend
> services, the React frontend, and (optionally) the monitoring stack via a
> separate profile. Health checks gate startup ordering.

### Frontend only

The `frontend/` directory is a standalone Vite + React + TypeScript app that
talks to the four backend services through a dev-server proxy (`vite.config.ts`).

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173  (proxies /api/* to the backend ports)
npm run build    # type-check + production build to dist/
npm run preview  # serve the production build
```

The proxy rewrites `/api/order → :18081`, `/api/inventory → :18082`,
`/api/delivery → :18083`, and `/api/notification → :18084`. Keycloak runs on
`:8080`; the Vite dev origin is whitelisted in the realm.

Full prerequisites and troubleshooting are added in the infrastructure milestone.

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
