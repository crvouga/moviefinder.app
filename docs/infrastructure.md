# Shared infrastructure integration

The authoritative guide is https://www.chrisvouga.dev/llms.txt. Re-fetch it before each infrastructure operation. Inventory in crvouga/workspace takes precedence. This repository never creates a Vault service, PostgreSQL server, Railway project, Cloudflare account, database/schema allocation, or control-plane credential.

## Verified ownership and current boundary

The target repository is `crvouga/moviefinder`. Its original checkout contained only SPEC.md. At implementation time, the published inventory has three older MovieFinder services owned by `moviefinder.app-rust`, `moviefinder.app-react`, and `moviefinder.app-clojurescript`. None names this repository, and no explicit `moviefinder` application schema allocation is recorded for this target. Those deployments have been preserved.

Shared development diagnostics read only application catalog metadata through `bun run db:check`. No shared PostgreSQL DDL or write has been performed by this implementation. Automated database verification uses disposable loopback PostgreSQL containers. An available shared DATABASE_URL is not allocation authorization.

The approved platform change must allocate the `moviefinder` schema in both development and production, with separate environment-selected connection contracts. The existing DATABASE_URL must remain opaque and must not be replaced globally to repurpose another app's database. All authentication, catalog, user, event, and collaboration tables live inside the allocated schema. No extension is required. Atlas revision metadata also stays in that schema.

## Secrets and plain configuration

| Name                             | Source / status                                                                              | Purpose                                               |
| -------------------------------- | -------------------------------------------------------------------------------------------- | ----------------------------------------------------- |
| DATABASE_URL                     | Existing Vault personal/dev and personal/prd contract; allocation not approved               | Opaque PostgreSQL connection                          |
| TMDB_API_READ_ACCESS_TOKEN       | Existing contract; development live read verified                                            | Server-only TMDB requests                             |
| BETTER_AUTH_SECRET               | Development value exists; register/reconcile key and used_by inventory for both environments | Session/auth cryptography                             |
| RESEND_API_KEY                   | Optional existing production key; absent in development                                      | Password recovery email                               |
| MOVIEFINDER_GOOGLE_CLIENT_ID     | Optional new registered app-specific key                                                     | Google OAuth client                                   |
| MOVIEFINDER_GOOGLE_CLIENT_SECRET | Optional new registered app-specific key                                                     | Google OAuth secret                                   |
| MOVIEFINDER_ORIGIN               | Plain environment; localhost:5173 for development, approved HTTPS origin for production      | Better Auth origin/callback/CSRF                      |
| MOVIEFINDER_DATABASE_ALLOCATION  | Plain confirmation of approved `moviefinder` allocation                                      | Migration guard; not a substitute for actual approval |
| EMAIL_FROM                       | Optional plain verified sender                                                               | Recovery mail delivery                                |
| PORT                             | Plain, 8080                                                                                  | Single public Bun port                                |

Reuse shared keys where appropriate; do not copy another app's OAuth credentials or authentication URL. New key names and environment parity must be registered through the workspace OpenTofu Vault inputs/inventory. Never print values. Runtime fleet services receive declared secrets directly from OpenTofu; they do not fetch Vault at boot. Local scripts use the supported vault run wrapper.

## Image and fleet contract

- Service ID proposed for infrastructure review: `moviefinder`; do not silently repurpose a legacy service.
- Source: https://github.com/crvouga/moviefinder, default branch main.
- Requested origin: `https://moviefinder.chrisvouga.dev`, subject to central allocation/ownership review.
- Image contract: `ghcr.io/crvouga/chrisvouga-moviefinder:<git-sha>` and latest through the reusable shared workflow.
- Dockerfile/context: `./Dockerfile`, `.`. Three build/runtime dependency stages plus pinned Bun image source; one final stateless Bun process, unprivileged user, port 8080.
- Liveness: `GET /health` → 200, independent of database, Vault, TMDB, and user authentication.
- Readiness: authenticated `GET /api/ready`; PostgreSQL unavailable → 503.
- Stateful work: PostgreSQL only; no disk, Redis, broker, R2, or Turborepo service required.
- SSE: yes; preserve streaming and disable response buffering. The server sends heartbeats and honors event cursors.
- Termination: SIGTERM cancels background work, closes live streams, drains HTTP, closes pool.
- Capacity: initially small personal deployment; bounded five-connection pool and two claimed jobs.

`.github/workflows/publish.yml` calls the platform's reusable GHCR publishing/deploy-dispatch workflow. Do not publish images manually, use Railway builds, run railway up, or change Railway/DNS outside inventory. Build without secrets. Runtime configuration is never copied into the image. The optional live smoke workflow uses GitHub OIDC with exportEnv:false and exposes only the requested key to its single command.

## Migrations and rollout

1. Infrastructure owners approve allocation and register required secret names/configuration. Record the approved change as evidence.
2. Confirm the selected dev/prd URL belongs to the app's allocated environment without logging it. Do not run local processes against production.
3. Review deterministic Atlas SQL and the additive migration strategy. Existing legacy apps/tables are not migrated, renamed, or removed by this change.
4. Run migration validation and status. Apply only with the approved allocation confirmation through `vault run`. Atlas is the sole migration authority, including Better Auth tables. Application startup never runs DDL.
5. Publish through the shared reusable workflow after source integration. Central OpenTofu selects that prebuilt image and binds only declared runtime variables.
6. Verify public liveness, authenticated readiness, registration/session reload, live catalog ingestion, and two-user list propagation at the approved origin.
7. Roll back application image through the central workflow if necessary. Additive database changes remain compatible; do not automatically drop user/catalog/auth tables to roll back.

The desired SQL model declares the schema for disposable Atlas normalization. Application migrations contain table changes only and assume the infrastructure-owned namespace already exists. Migration generation uses a disposable database populated by committed Atlas migrations, then Atlas schema diff; it does not require a paid baseline feature and does not inspect shared data.

## Missing tables during local startup

`relation "moviefinder.user" does not exist` means the selected PostgreSQL server is reachable but the app's authentication table is absent. It does not mean an existing account's password is wrong. `bun run db:check` reports missing application tables/columns using read-only catalog queries and does not expose the connection string. `bun run dev` runs this check before starting either server.

The inventory was re-fetched on 2026-10-16 and still has no approved `database_schema` allocation for `crvouga/moviefinder`. The three legacy MovieFinder services are separate repositories. Complete the prepared allocation request through the shared platform; only after approval may the existing guarded Atlas command apply migrations. Do not set its confirmation flag to work around missing authorization, create the namespace manually, or redirect another application's connection contract.

## Hosting request

A complete request is maintained in docs/hosting-request.md after container verification. Check existing workspace issues before submission. Deployment remains unverified until an approved inventory change, available published image, required runtime secrets, applied shared migrations, and live health smoke have all succeeded. Local image or fixture tests are not deployment evidence.
