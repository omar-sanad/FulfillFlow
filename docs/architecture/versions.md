# Version selection

This document records the selected dependency versions for FulfillFlow and the
rationale for choosing mutually compatible, stable releases.

## Principles

1. Use only supported, stable releases -- not betas or release candidates.
2. Ensure mutual compatibility across the Spring Boot, Kafka, PostgreSQL, and
   Keycloak components.
3. Prefer LTS runtimes (Java 21 LTS).
4. Pin important CI action versions to major version tags.
5. Record versions in the root README and here.

## Selected versions

### Backend

| Component | Version | Notes |
|---|---|---|
| Java | 21 (LTS) | Current supported LTS; required by Spring Boot 3.x |
| Spring Boot | 3.3.x | Bundles compatible Spring framework, Spring Security, Spring Data JPA, Spring for Apache Kafka |
| Spring for Apache Kafka | (managed by Spring Boot BOM) | Compatible with Kafka 3.7 clients |
| Flyway | (managed by Spring Boot BOM) | Schema migrations |
| MapStruct | 1.5.x | Optional mapping; only if complexity justifies |
| Lombok | optional | Only if it improves readability without hiding behavior |

### Infrastructure

| Component | Version | Notes |
|---|---|---|
| Apache Kafka | 3.7.x | KRaft mode (no ZooKeeper) |
| PostgreSQL | 16 | Per-service databases |
| Keycloak | 25.x | OAuth2/OIDC identity provider |
| Docker Compose | v2 | Local orchestration |
| Prometheus / Grafana | latest stable | Monitoring profile |
| OpenTelemetry Collector | latest stable | Tracing pipeline |
| Jaeger (or Grafana Tempo) | latest stable | Trace storage/UI |

### Frontend

| Component | Version | Notes |
|---|---|---|
| Node | 20 LTS | Build/runtime |
| TypeScript | 5.4.x | Strict mode |
| React | 18.x | UI library |
| Vite | 5.x | Build tool / dev server |
| React Router | 6.x | Routing |
| TanStack Query | 5.x | Server state |
| React Hook Form | 7.x | Forms |
| Zod | 3.x | Schema validation |
| Vitest + Testing Library | latest stable | Component tests |
| Playwright | latest stable | E2E tests |

### Testing (backend)

| Component | Version | Notes |
|---|---|---|
| JUnit 5 | (managed by Spring Boot BOM) | Test framework |
| AssertJ | (managed by Spring Boot BOM) | Fluent assertions |
| Mockito | (managed by Spring Boot BOM) | Isolation only where appropriate |
| Testcontainers | 1.19.x | Real Kafka + PostgreSQL in tests |
| Awaitility | (managed by Spring Boot BOM) | Async assertions |
| REST Assured / MockMvc | latest stable | REST endpoint testing |
| ArchUnit | 1.x | Architectural rules |

## Compatibility notes

- Spring Boot 3.3.x requires Java 17+; we target Java 21 LTS.
- Spring Boot 3.3.x's managed Kafka client version is compatible with a Kafka
  3.7 broker running in KRaft mode.
- Keycloak 25 ships with a compatible Keycloak adapter model; Spring Boot
  services validate JWTs as OAuth2 Resource Servers (no Keycloak-specific adapter
  needed).
- Testcontainers Kafka and PostgreSQL modules are compatible with the selected
  broker and database versions.

## Verification

Versions will be re-verified at each milestone's dependency selection step
(especially the service foundations and infrastructure milestones) and updated
here and in the root README if a patch release is required for compatibility.
