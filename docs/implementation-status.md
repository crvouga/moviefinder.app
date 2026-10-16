# Implementation and verification status

Updated on 2026-10-16 (America/Phoenix). This is an implementation handoff, not a claim of completed production deployment.

## Delivered application

The repository now contains a functioning TypeScript/Bun application rather than only a specification. React 19, Vite+, TanStack Router, Tailwind/shadcn-style Radix primitives, Swiper, TanStack DB Query Collections, Hono RPC, Better Auth, Drizzle, PostgreSQL, Atlas, Zod, and Playwright are integrated.

Implemented vertical features include protected registration/login/logout/session restoration and deep-link return; recovery-token issuance/consumption and session revocation; public profile editing and avatars; movie/TV discovery and composable/saved feeds; cursor pagination and global merging of sorted movie/TV source streams; independent optimistic viewing/opinion tags; public lists with stable item identities, editing, reorder handles/arrows, invitations/acceptance/decline, owner/editor permissions, copies and saves; user follows, activity, global catalog/social search, and scoped/paginated list/profile reads.

Movie, TV, season, episode, person, credit, and genre projections retain navigable canonical relationships. Details expose source metadata, cast/crew, recommendations, trailers, external links, TV seasons/episodes, person filmographies by department, and sourced streaming availability. Missing metadata remains absent or explicitly unavailable.

The selective source replica retains JSONB, revisions/checksums, projection-specific fetch metadata, adaptive deadlines, source coverage, durable jobs/leases, and persisted upstream cooldown checkpoints. Query definitions separate cache reads, scheduling, and normalization. SSE includes durable ordered replay, snapshots, session checks, heartbeats, duplicate/stale-revision handling, and shutdown cleanup. Commands commit their operation receipt and event with authoritative state. A per-user browser outbox protects queued command intent across reload without storing authentication secrets or duplicate server entities.

## Files and boundaries

- `src/server/db`, `atlas/schema.sql`, `atlas/migrations`, `atlas.hcl`: isolated schema mapping and Atlas authority.
- `src/server/entity-store`, `entity-store-sync`, `queries`: raw replica, normalization, provider ingestion/jobs/coverage, and declarative query definitions.
- `src/server/auth.ts`, `app.ts`, `commands.ts`, `events.ts`, `social.ts`, `state.ts`, `static.ts`, `index.ts`: persisted authentication, typed APIs, authorization/concurrency, streaming/replay, public projections, and single-process serving.
- `src/client`: canonical collections, query/subscription merging, optimistic commands/outbox, routing, code-split mobile UI, and accessible sheets/navigation.
- `src/shared`: validated commands/feeds/events, identities, filter semantics, genre taxonomies, and URL serialization.
- `scripts`, `tests`, `.github/workflows`, `Dockerfile`: development supervision, isolated PostgreSQL tests, Atlas verification, live catalog/container smoke, and centralized publishing configuration.
- `README.md`, `AGENTS.md`, and `docs/*`: setup, actual architecture/contracts, verification, and prepared infrastructure handoff.

The supplied SPEC.md is unchanged. No pre-existing application code, legacy deployment, shared database, shared policy, or infrastructure allocation was overwritten. Development database diagnostics read application catalog metadata only; shared DDL and writes remain unperformed.

## Verification evidence

| Command / check                                                     | Actual result                                                                                                                                       |
| ------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- |
| `bun run check`                                                     | Passed: Oxfmt, Oxlint, native type checking with no warnings/errors; 24 unit/property tests                                                         |
| `bun run build`                                                     | Passed: code-split production assets; no oversized-chunk warning                                                                                    |
| `bun run test:integration`                                          | Passed: 38 tests against fresh disposable PostgreSQL with real Atlas migrations and Better Auth                                                     |
| `bun run test:e2e`                                                  | Passed: 20 tests across mobile and desktop Chromium with real Bun/Hono/PostgreSQL and deterministic upstream fixtures                               |
| `bun run db:validate`                                               | Passed: all committed migration hashes valid                                                                                                        |
| `bun run db:verify`                                                 | Passed: fresh Atlas replay and desired-schema comparison report no schema changes                                                                   |
| `vault run -- bun scripts/smoke.ts`                                 | Passed against live TMDB: movie, TV, season, episode, and person source documents validate and normalize; no database writes                        |
| `docker build --platform linux/amd64 -t moviefinder:verification .` | Passed: image built, 89,073,376 bytes (84.9 MiB)                                                                                                    |
| Native ARM64 image build and container smoke                        | Passed: `/health` 200 with PostgreSQL unavailable, `/auth` 200, protected API 401, protected deep link 302; final image 90,392,880 bytes (86.2 MiB) |
| Local emulated x64 container execution                              | Failed inside Bun 1.4.2 before application startup, with no AVX support reported by the emulator; not counted as a successful runtime check         |
| Native x64 CI / public live deployment                              | Not executed; required before production completion                                                                                                 |

Browser coverage includes registration and reload, touch/wheel poster navigation with bounded slide nodes and position restoration, correct return to a protected deep link, independent tags across navigation, list creation/add/reorder/reload, TV/season/episode navigation, search/profile persistence, two-user concurrent additions and reconnect recovery, combined filters/URL serialization, optimistic rollback after 403, recovery after a temporary initial snapshot failure, saved feeds, list copies, invitation acceptance, no-op command persistence, and ordered recovery of dependent offline commands. Unit/integration coverage includes provider cooldown across workers, expired lease recovery, partial movie coverage when TV fails, dense ordering properties, event replay/reset, and unchanged-ingestion idempotency.

The main browser entry is approximately 296 KB before gzip (95 KB gzipped), with shared dependencies and the authenticated data layer split into separate chunks. This is a bundle measurement, not a claim of performance testing on a physical handset. Poster dimensions, bounded adjacent prefetch, and virtual slide rendering are implemented; physical-device performance validation remains unperformed.

## Migration status

Three versioned migration files are included in the implementation commit:

1. `202610160001_initial.sql`: application/auth/catalog/social/collaboration/event tables and indexes; assumes an already allocated schema.
2. `20261016051851_projection_freshness.sql`: additive detail-fetch timestamp.
3. `20261016053809_durable_provider_cooldown.sql`: additive synchronization checkpoint table.

The latter migrations were generated by Atlas. All three applied successfully to fresh disposable PostgreSQL; the desired SQL model matches the replayed schema. **No shared development or production migration was applied.** Atlas revision metadata is configured inside the application's namespace. The guarded apply command is not allocation authorization.

## External blockers and unverified acceptance criteria

- No approved PostgreSQL/schema allocation or fleet service for `crvouga/moviefinder` is recorded in the checked shared inventory. Existing MovieFinder services belong to different repositories and remain intact. The central OpenTofu process must approve this repository's role and allocate its resources before shared DDL/writes or rollout.
- `BETTER_AUTH_SECRET` exists in development but its required inventory/production registration must be reconciled. Runtime secrets and the app-specific public origin must be declared through the shared workflow.
- Password recovery delivery requires `RESEND_API_KEY` and verified `EMAIL_FROM`; development lacks the mail key. The real token/reset/revocation logic is tested with captured delivery, not a live email. Optional Google login remains disabled without app-specific credentials/callback configuration.
- The implementation and migration history are committed on the local feature branch. Upstream source/image integration and native x64 CI remain outstanding. Publishing is configured exclusively through the documented reusable GHCR workflow; no manual image push, Railway setup, or shared resource change was performed. No public health endpoint for this new implementation has been verified.
- The local x64 emulator crashes in Bun itself. Native ARM execution passes; native x64 runtime validation on the configured runner is still required. No runtime substitution or check suppression was used.

The complete request body is [hosting-request.md](hosting-request.md); setup and rollout instructions are [infrastructure.md](infrastructure.md). The request has not been submitted because the guide's upstream source/published-image prerequisites are still pending. No approval, attestation, production deployment, or external verification is implied by local test success.

## Product/source limits

TMDB has distinct film and TV genre categories and lacks some TV cast/crew discovery constraints. The app reports affected combinations, uses cached credits where available, and avoids claiming unavailable metadata is complete. Random and personalized feeds rank bounded provider candidates; they do not claim a uniform sample or exhaustive ordering of the full TMDB catalog. Personalization uses actual genre preferences rather than invented recommendations.

The initial architecture deliberately serializes committing writes and polls PostgreSQL events. It retains durable event/operation history without automatic destructive pruning. Measure workload before scaling replicas or adding a reviewed retention strategy. Shared deployment, mail delivery, OAuth, physical-handset testing, and native x64 runtime checks must remain unchecked until real evidence exists.

## Local missing-table follow-up (2026-10-16)

The reported `relation "moviefinder.user" does not exist` error was reproduced against disposable PostgreSQL. A read-only check of the selected Vault development database confirms that required application tables/columns are absent. The current inventory was re-fetched and still records no schema allocation for this repository. No shared migration was applied.

Development now checks all required tables/columns before starting either server and exits with migration/allocation instructions when they are missing. Database readiness uses the same check; public liveness stays independent. Missing-schema/permissions errors pause synchronization until restart; transient connection failures back off with bounded logs. Unexpected Better Auth errors go through Hono's sanitized 503 response instead of printing SQL parameters. Regression tests cover missing authentication tables, additive columns, expected invalid-credential responses, read-only metadata checks, worker pause, and transient recovery.

The super.engineering Run menu includes **Database check** (read only) and **Migrate database** (the existing allocation-guarded Atlas command). Login remains blocked on the external schema allocation and subsequent authorized migrations. A credential being available in Vault is not evidence that the application schema has been provisioned.
