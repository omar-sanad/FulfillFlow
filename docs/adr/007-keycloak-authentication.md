# ADR-007: Authentication with Keycloak

- **Status:** Accepted
- **Date:** Milestone 0

## Context

The platform has four roles (Customer, Administrator, Warehouse, Courier) with
distinct capabilities, plus ownership-level rules (customers see only their own
orders/notifications; couriers modify only their own deliveries). A robust
identity and authorization layer is required, and building one from scratch is
out of scope for a portfolio project.

## Decision

Use **Keycloak** as the OAuth2 / OpenID Connect identity provider.

- The React frontend uses Authorization Code flow with PKCE (public client).
- Backend services validate JWT access tokens as Spring OAuth2 Resource Servers.
- Roles are encoded in token claims; authorization is enforced server-side.
- Identity (subject, roles) is derived from validated token claims, never from
  request bodies.
- A development realm with synthetic users for each role is provisioned.
- CORS is configured narrowly; security headers are applied.
- No real credentials are committed; development-only credentials live in
  `.env.example`.

## Alternatives considered

- **Custom JWT issuance:** Re-implements identity, token rotation, and session
  management; high risk and out of scope.
- **Spring Authorization Server:** Viable, but Keycloak provides a richer admin
  UI and realm management suitable for demonstration.
- **API keys:** Too weak for role-based and ownership-level authorization across
  a browser frontend.

## Consequences

- **Positive:** Mature, standards-based authn/authz; clear separation of identity
  from application code.
- **Negative:** Keycloak adds an infrastructure dependency and startup time.
- **Neutral:** Resource servers depend on Keycloak's JWKS endpoint; tokens are
  validated offline after key fetch.
