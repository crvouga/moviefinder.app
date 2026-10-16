# Verification

Run `bun install --frozen-lockfile` once. `bun run check` uses Vite+ for formatting, type-aware lint, native TypeScript checking, and fast unit/property tests. There is no independent ESLint, Prettier, tsc, or standalone Vitest pipeline.

`bun run test:integration` starts an ephemeral PostgreSQL 17 container, creates only its local test schema, applies the committed Atlas migration, runs the integration project through Vite+, and stops the container in finally. An explicit loopback TEST_DATABASE_URL with a database name ending `_test` can reuse a disposable server. Shared DATABASE_URL is never used.

`bun run build && bun run test:e2e` verifies the real compiled UI against Bun/Hono, persisted Better Auth, actual PostgreSQL, and deterministic TMDB fixtures. Install the matching browser with `bun x playwright install chromium` when necessary. Mobile and desktop projects cover registration, session reload, tags, catalog navigation, list editing, profile updates, searching, and cross-user SSE/reconnect. Browser failure evidence stays in ignored test-results; it contains test accounts only.

Unit/property coverage exercises canonical identity, missing-field normalization, credit/season/episode links, canonical serialization, compound filter/cursor validation, permissions, dense ordering keys, redirect safety, replay revision gating, and upstream Retry-After/errors.

Integration coverage uses real transactions for authentication tables, unauthorized APIs/origins, field independence, operation deduplication, list permissions/invitations, concurrent additions/moves, public copies, audience-filtered replay, cache-first query hydration, durable request deduplication, preservation of hydrated source data, and transactional snapshots.

`bun run db:validate` checks migration hashes. Atlas application is also exercised on a fresh database by each isolated test run. Docker verification must build the actual multi-stage image, run its Bun process with runtime configuration, and request `/health`. A live TMDB smoke test is separate from deterministic suites and must report a missing credential or upstream error as a missing check.

CI separates fast required checks and PostgreSQL integration from scheduled/manual deeper browser and container checks. The optional live catalog workflow obtains only the TMDB variable via short-lived GitHub OIDC; no persistent repository Vault token is used.

Actual verification results and any outstanding limits are recorded in docs/implementation-status.md at handoff. Do not infer successful production deployment from local fixture or container tests.

The container smoke helper defaults to linux/amd64; set CONTAINER_PLATFORM=linux/arm64 only to verify a native ARM image. An ARM smoke pass must never be reported as an x64 runtime pass. Production dependency installation omits unused optional/peer packages; the final runtime selects the target Bun architecture.
