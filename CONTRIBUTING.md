# Contributing to FulfillFlow

Thank you for your interest in contributing to FulfillFlow. This is a portfolio
project, but contributions are welcome.

## Project context

FulfillFlow is an event-driven order-fulfilment and delivery-management
platform. It is a synthetic portfolio artifact -- all data is fabricated. See the
root [README](README.md) for the architecture and milestone plan.

## Development principles

Before contributing, please read and follow the development principles in the
project specification. In particular:

- Use only synthetic products, users, orders, and operational data.
- Do not include code, names, data, or business rules belonging to any real
  employer.
- Keep business logic separate from framework and infrastructure code.
- Do not leave TODO methods, fake implementations, or tests that always pass.
- Never suppress a failing test to obtain a green build.
- Do not commit secrets, generated binaries, IDE metadata, or local environment
  files.
- Use UTC for persisted timestamps and ISO 8601 in APIs and events.
- Use UUIDs for externally visible identifiers.

## Getting started

```bash
make setup   # one-time preparation
make start   # start the local stack
make test    # run tests
make stop    # stop the stack
make clean   # remove generated artifacts (warns before deleting data)
```

Review [`.env.example`](.env.example) for required environment variables.

## Workflow

1. Open an issue describing the change before starting significant work.
2. Create a feature branch from `main`.
3. Make focused, minimal changes.
4. Run the relevant tests and ensure they pass.
5. Keep commits focused -- one logical change per commit.
6. Open a pull request describing the change and linking the issue.

## Commit style

- Use conventional commit prefixes: `feat:`, `fix:`, `chore:`, `docs:`, `test:`,
  `ci:`, `refactor:`.
- Keep the subject line short and imperative.
- Reference issues in the body where relevant.

## Code quality gates

The build must remain green. CI fails for:

- Compilation errors
- Failing tests
- Formatting violations
- Important static-analysis violations
- Broken frontend type checking
- Broken production build

Do not commit if any gate is failing. Fix the issue rather than suppressing it.

## Testing

- Use real Kafka and PostgreSQL via Testcontainers for integration tests.
- Use Awaitility for asynchronous assertions; avoid arbitrary long sleeps.
- Do not mock Kafka in tests intended to prove Kafka integration.
- Prioritize domain logic and critical workflows over vanity coverage numbers.

## Reporting security issues

See [SECURITY.md](SECURITY.md). Do not open public issues for security
vulnerabilities.

## License

By contributing, you agree that your contributions are licensed under the MIT
License covering this repository.
