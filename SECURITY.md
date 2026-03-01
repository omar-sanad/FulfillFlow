# Security Policy

## Supported versions

FulfillFlow is a portfolio project. Security updates are applied to the latest
`main` branch only. There are no versioned releases requiring backport support.

## Reporting a vulnerability

If you discover a security vulnerability, please report it responsibly:

1. Do **not** open a public GitHub issue.
2. Email the repository owner directly with a description of the vulnerability,
   reproduction steps, and impact assessment.
3. Allow a reasonable response window before any public disclosure.

We will acknowledge receipt and work with you to understand and address the
issue.

## Security model

FulfillFlow uses OAuth2 / OpenID Connect via Keycloak:

- The React frontend uses Authorization Code flow with PKCE.
- Backend services validate JWT access tokens as OAuth2 Resource Servers.
- Role-level and ownership-level authorization is enforced server-side.
- Identity is derived from validated token claims, never from request bodies.
- CORS is configured narrowly; security headers are applied.
- Couriers cannot modify deliveries assigned to another courier.
- Warehouse users cannot access administrator-only functionality.
- Customers can access only their own orders and notifications.

See [`docs/architecture/security.md`](docs/architecture/security.md) (added in
the identity milestone) for details.

## Secrets handling

- **No secrets are committed to this repository.**
- All credentials in [`.env.example`](.env.example) are development-only and
  synthetic.
- Real keys, tokens, and passwords must remain in your local `.env` file (which
  is git-ignored) or in a secure secret manager.
- Logs and error messages must never expose full tokens, stack traces, or
  sensitive personal data.
- Notification providers are simulated -- no real email or SMS is ever sent.

## Dependency security

- CI runs dependency and security scanning where practical.
- Dependencies are pinned to supported, mutually compatible stable versions (see
  [`docs/architecture/versions.md`](docs/architecture/versions.md)).
- Supply-chain actions are pinned to major version tags.

## Production considerations

Local Docker Compose and Kubernetes manifests are for demonstration only. For
any real deployment:

- Use managed PostgreSQL and a managed Kafka-compatible platform.
- Use a managed Keycloak or hardened OIDC provider.
- Store secrets in a secret manager, not in images or ConfigMaps.
- Run containers as non-root users (the provided Dockerfiles enforce this).
- Apply network policies and least-privilege RBAC in Kubernetes.
