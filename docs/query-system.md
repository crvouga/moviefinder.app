# Query system

`defineQuery` separates parameter validation, cache reading, background scheduling, and normalization. Execution reads PostgreSQL immediately and schedules durable work; it does not await an upstream HTTP call. Request cancellation prevents publishing an abandoned request response. Shared durable hydration is not canceled merely because one viewer navigates away.

Discovery parameters compose media type, included/excluded genres, dates/years, runtime, language, country, rating/votes/popularity, cast/crew, keywords, providers/region, viewing status, sentiment, list membership, query text, and sort order. Zod validates HTTP inputs. Parameter serialization sorts object keys. Cursors bind their page to the filter identity; a cursor from a different feed is rejected.

Provider-supported constraints are translated to TMDB discovery parameters. Viewing status, sentiment, membership, current-session exclusions, and popularity bounds are applied against available local state. The protocol reports unsupported metadata combinations, including TMDB's lack of TV cast/crew discovery constraints. No source-coverage claim is made from a partial local fallback.

The first response includes entities, ordered primary IDs, a stable query ID, and coverage. A missing page can still contain clearly partial cached matches. Once upstream ingestion commits, normalized entity deltas arrive over SSE. The completion signal causes an active query to read authoritative coverage and IDs again. Query metadata is itself a TanStack DB entity.

Pagination follows provider page limits and continues even when a locally filtered page is empty. IDs are deduplicated across loaded pages. For combined movie/TV sorts, a buffered merge preserves global source ordering across output pages, carrying the unread lane buffers in durable coverage state. Random and personalized ranking use a bounded provider candidate window; random discovery retains its URL seed, and personalization scores genre preferences from actual likes/dislikes. Saved feed parameters are persisted per user; useful active filters are reflected in the URL.

Search immediately renders matching canonical cached entities, then supplements them through the same durable query/ingestion system. Movie, TV, and person results have navigable identities. User/list search uses public normalized projections.

Query counters, latency, cache hits/misses, provider requests/rate limits, and failed jobs are exposed through authenticated metrics. Source responses and errors never expose provider credentials.

## Anticipatory navigation

Authenticated navigation warms screen code, canonical collection data and title
artwork on pointer hover, keyboard focus and primary press-down. A viewport
predictor warms four likely visible destinations; discovery additionally warms
the current title, three upcoming titles and the previous title, and requests
the next result page twelve slides before the end. Predicted requests share a
three-request budget. Actual navigation promotes queued work immediately and
joins in-flight reads. Route loaders prepare the module and first cache response
before committing the destination.

Request metadata retains query IDs only; all entities remain in TanStack DB.
Incomplete speculative coverage is refreshed for at most fifteen seconds while
interest remains. Leaving an intent, moving predictions out of view, changing
routes, hiding the tab or signing out releases that interest and aborts reads
with no remaining consumers. Shared server hydration jobs remain durable.
Completed request metadata expires quickly and is invalidated by committed
commands/SSE events. Save-Data and 2G connections disable viewport predictions.
Cold provider misses and upstream outages can still produce progressive content.
