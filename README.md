# MovieFinder

A mobile-first home for finding films and TV, recording your taste, and making public collections with friends. Discovery uses a virtualized vertical Swiper feed, independent seen/sentiment controls, composable filters, and progressive catalog updates. The interface has no loading spinners.

## Run locally

Requires Bun 1.4.2, a supported Node runtime (22.22.3 used here), Atlas, Docker for isolated tests, and the shared platform's installed `vault` wrapper. Do not install a separate Vault or PostgreSQL platform.

1. Read https://www.chrisvouga.dev/llms.txt and obtain an approved MovieFinder allocation through crvouga/workspace.
2. Authenticate using the documented `vault login` workflow. This checkout's `.vault.yaml` selects `personal/dev`.
3. `bun install --frozen-lockfile`.
4. After allocation approval, run `bun run db:status` and `bun run db:migrate` with the non-secret `MOVIEFINDER_DATABASE_ALLOCATION=moviefinder` confirmation supplied by the approved environment.
5. Set the plain `MOVIEFINDER_ORIGIN` to `http://localhost:5173`, then run `bun run dev`.

The browser uses port 5173. Vite+ proxies API requests to Bun on 8080. `bun run dev` injects development Vault secrets, checks the required database tables and columns without writing, then starts both processes. If migrations are missing, it exits with the setup steps before starting either server. `dev:local` is for an environment that has already injected its configuration. No CORS setup is needed.

Required runtime variables are `DATABASE_URL` and `BETTER_AUTH_SECRET`. Production also requires the app-specific `MOVIEFINDER_ORIGIN`. `TMDB_API_READ_ACCESS_TOKEN` enables catalog refresh; cached content remains usable during an upstream outage. `.env.example` lists variable names without values. Never reuse another application's authentication base URL.

Email/password registration works without a social provider. Recovery is enabled only with `RESEND_API_KEY` and a verified `EMAIL_FROM`. Google login appears only with app-specific `MOVIEFINDER_GOOGLE_CLIENT_ID` and `MOVIEFINDER_GOOGLE_CLIENT_SECRET`. These integrations require registered infrastructure secrets and callback/sender configuration.

## super.engineering hooks

The repository's `.super.engineering/config.json` provides the built-in hooks:

- **Setup** runs `bun install --frozen-lockfile` when super.engineering sets up the checkout.
- **Run → MovieFinder** is the default. It runs `bun run dev`, loads development secrets through Vault, and starts the frontend and backend together. Open `http://localhost:5173`.
- **Run → Checks** runs the formatting, lint, native type checking, and unit/property suite with `bun run check`.
- **Run → Database check** checks the development database tables/columns without writing or applying migrations.
- **Run → Migrate database** runs the guarded Atlas migration command after the infrastructure allocation has been approved and its confirmation configured.
- **Stop** terminates the selected Run process. The development supervisor handles termination of both development servers.

Authenticate with `vault login` and complete the approved database allocation/migration steps above before running MovieFinder. Setup and MovieFinder never apply migrations; the separate migration entry keeps the existing allocation guard. Keep runtime files and secrets out of the shared hook configuration.

If logs say `relation "moviefinder.user" does not exist`, PostgreSQL is reachable but the authentication tables have not been created in the selected database. Run **Database check** (or `bun run db:check`) for a read-only diagnosis. Obtain the approved `moviefinder` schema allocation through `crvouga/workspace`, then run **Migrate database** (or `bun run db:migrate`) and restart MovieFinder. An available `DATABASE_URL` or a missing table does not authorize creating shared infrastructure, switching databases, or bypassing the allocation guard. The current inventory does not yet record an allocation for this repository; the complete change request is in `docs/hosting-request.md`.

## Commands

| Command                             | Purpose                                                                    |
| ----------------------------------- | -------------------------------------------------------------------------- |
| `bun run check`                     | Oxfmt, Oxlint, native TypeScript 7, unit/property tests through Vite+      |
| `bun run format`                    | Format through Vite+                                                       |
| `bun run lint`                      | Lint through Vite+                                                         |
| `bun run build`                     | Production React bundle through Vite+                                      |
| `bun run test:integration`          | Disposable PostgreSQL, Atlas migrations, API/concurrency integration suite |
| `bun run test:e2e`                  | Disposable PostgreSQL and Playwright mobile/desktop; build first           |
| `bun run db:validate`               | Validate Atlas migration integrity                                         |
| `bun run db:check`                  | Read-only development database table/column check through Vault            |
| `bun run db:diff change_name`       | Generate an Atlas migration from atlas/schema.sql                          |
| `bun run db:migrate`                | Apply through Vault after the allocation guard passes                      |
| `bun run db:status`                 | Read Atlas migration status through Vault                                  |
| `bun run db:drift`                  | Compare live application schema with the desired SQL schema                |
| `vault run -- bun scripts/smoke.ts` | Explicit live TMDB smoke check; no database writes                         |

The test scripts refuse non-loopback test URLs or database names that do not end in `_test`. Fixture upstream responses are used only by tests. Production has no mock catalog or authentication bypass.

## Architecture

One Bun/Hono process serves React, authenticated APIs, Better Auth, and SSE. Drizzle queries the allocated `moviefinder` PostgreSQL schema; Atlas owns its migrations. `entity-store` owns source JSON storage and query-time normalization. `entity-store-sync` owns every TMDB request, durable jobs, rate limiting, and refresh policies. TanStack DB query collections hold normalized browser entities and optimistic transactions.

The database retains events in the same transactions as mutations. Replay uses a global ordered cursor. A transaction-level PostgreSQL advisory lock makes event identity reflect commit order and gives item-level moves deterministic serialization. Stable item IDs and fractional keys avoid whole-list replacement.

See [architecture](docs/architecture.md), [entity store](docs/entity-store.md), [queries](docs/query-system.md), [realtime](docs/realtime.md), [testing](docs/testing.md), and [infrastructure](docs/infrastructure.md).

## Deployment status

The application is prepared for the shared GHCR/Railway workflow. **It has not been deployed to shared infrastructure.** At implementation time, the inventory contains older MovieFinder services belonging to other repositories, but no service or explicit schema allocation for `crvouga/moviefinder`. Shared migrations are intentionally unapplied. See [infrastructure](docs/infrastructure.md) for the prepared integration contract and external requirements.

Build the container with `docker build -t moviefinder:verification .`. Runtime secrets are supplied as environment variables, never build arguments. `/health` is public and independent of PostgreSQL. `/api/ready` and `/api/metrics` require authentication. See the infrastructure document before attempting rollout.

This product uses the TMDB API but is not endorsed or certified by TMDB. Streaming availability is attributed to JustWatch through TMDB.
