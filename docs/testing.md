# Verification

Run `bun install --frozen-lockfile` once. `bun run check` uses Vite+ for formatting, type-aware lint, native TypeScript checking, and fast unit/property tests. There is no independent ESLint, Prettier, tsc, or standalone Vitest pipeline.

`bun run test:integration` starts an isolated embedded PostgreSQL cluster in a temporary directory, applies the committed Atlas migrations, runs the integration project through Vite+, then stops and removes only that temporary cluster. An explicit loopback TEST_DATABASE_URL with a database name ending `_test` can reuse a disposable server (CI also checks against PostgreSQL 17). Shared DATABASE_URL is never used.

`bun run build && bun run test:e2e` verifies the real compiled UI against Bun/Hono, persisted Better Auth, isolated embedded PostgreSQL, and deterministic TMDB fixtures. Install the matching browser with `bun x playwright install chromium` when necessary. Mobile and desktop projects cover registration, session reload, tags, catalog navigation, list editing, profile updates, searching, and cross-user SSE/reconnect. Browser failure evidence stays in ignored test-results; it contains test accounts only.

Unit/property coverage exercises canonical identity, missing-field normalization, credit/season/episode links, canonical serialization, compound filter/cursor validation, permissions, dense ordering keys, redirect safety, replay revision gating, and upstream Retry-After/errors.

Integration coverage uses real transactions for authentication tables, unauthorized APIs/origins, field independence, operation deduplication, list permissions/invitations, concurrent additions/moves, public copies, audience-filtered replay, cache-first query hydration, durable request deduplication, preservation of hydrated source data, and transactional snapshots.

`bun run db:validate` checks migration hashes. Atlas application is also exercised on a fresh database by each isolated test run. Docker verification must build the actual multi-stage image, run its Bun process with runtime configuration, and request `/health`. A live TMDB smoke test is separate from deterministic suites and must report a missing credential or upstream error as a missing check.

CI separates fast required checks and PostgreSQL integration from scheduled/manual deeper browser and container checks. The optional live catalog workflow obtains only the TMDB variable via short-lived GitHub OIDC; no persistent repository Vault token is used.

Actual verification results and any outstanding limits are recorded in docs/implementation-status.md at handoff. Do not infer successful production deployment from local fixture or container tests.

The container smoke helper defaults to linux/amd64; set CONTAINER_PLATFORM=linux/arm64 only to verify a native ARM image. An ARM smoke pass must never be reported as an x64 runtime pass. Production dependency installation omits unused optional/peer packages; the final runtime selects the target Bun architecture.

`bun run test:local-database` exercises the real embedded PostgreSQL package in a temporary directory: Atlas bootstrap, Better Auth registration, safe attachment to a running local cluster, shutdown, persistence across restart, and rejection of a nonlocal migration target. It does not use Vault, shared DATABASE_URL, Docker, or the developer's persistent `.local/` data. CI runs this alongside the existing PostgreSQL integration suite. Platform binary postinstall scripts are explicitly trusted so package-required library symlinks are generated on fresh installs.

Local schema generation/verification also uses embedded PostgreSQL: committed migrations replay into a temporary application database and Atlas normalizes the desired schema in a separate temporary scratch database. Both are cleaned up. This keeps Atlas diff/checks independent of Docker and the developer's `.local/` cluster. Docker remains necessary only for container-image verification.
