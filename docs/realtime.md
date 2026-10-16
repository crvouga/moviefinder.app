# Realtime and collaboration

All commands use authenticated HTTP, not SSE. The stream is server-to-client delivery of committed changes.

## Protocol v1

`/api/events` sends `change` events with SSE `id` and a JSON envelope containing `protocol`, global `id`, event `type`, subscription identity, normalized entities, removed kind/ID pairs, and timestamp. Types include `upsert`, `remove`, `reset`, and `complete`; removals can accompany an upsert transaction. Heartbeats are emitted every 15 seconds. Cache-control and proxy buffering headers preserve streaming.

The bootstrap snapshot returns a replay cursor from the same repeatable-read database snapshot. Clients connect from that cursor. EventSource supplies Last-Event-ID on reconnect; the header takes precedence over the initial query cursor. Replay reads ascending event IDs in bounded batches. Audience filtering prevents delivery of another user's saved feed/follow preferences. Session validity is rechecked during long-lived streams.

If a retained event floor is newer than the supplied cursor, the server sends a consistent snapshot reset and resumes from its watermark. Clients process events sequentially, ignore already processed global IDs, reject older entity revisions, and retain tombstones for removed stable IDs. Snapshot replacement clears obsolete canonical state before merging the recovery snapshot.

Events are currently retained durably without automated deletion. Operators may introduce a reviewed retention job; the stale-cursor path already handles a retained floor. Operation receipts must remain longer than the accepted retry horizon. Do not delete either log casually.

## Deterministic conflict rules

A PostgreSQL transaction advisory lock is taken before mutating application state or emitting catalog events. Writers hold it through commit; consequently event IDs cannot skip an uncommitted lower-ID event and later make a replay client miss it. This also serializes duplicate command IDs across processes.

An operation receipt is keyed by user plus operation UUID and records a payload fingerprint and result. Exact retries return the same result. Reusing the UUID for a different command returns 409. State, activity, event, and receipt commit atomically.

Statuses patch only supplied dimensions. Concurrent seen and sentiment changes therefore survive independently; concurrent changes to the same field resolve in server commit order.

Lists use stable item IDs and fractional ordering keys with bytewise PostgreSQL collation. A move changes only the selected item and computes a key between current neighbors after serialization. Distinct concurrent additions survive; duplicate title additions are constrained to one row per list/title. A move relative to a removed anchor appends; moving an already removed item returns 404. The last committed move of the same item determines its position. A removed collaborator loses authorization for all subsequent commands.

Owners manage metadata/collaborators; editors change contents/order; authenticated viewers can read/copy/save. Invitations can be accepted or declined only by their recipient. Owners cannot remove themselves through editor removal.

Client-created lists and items use stable operation UUIDs as their identities. A per-user browser outbox persists only pending command intents, never authentication secrets or copies of server entities. Reload recovery reuses operation IDs and restores optimistic overlays; receipts make committed retries harmless. Permanent command failures retire the intent and roll back the overlay. Browser keepalive also allows a submitted command to finish during navigation.
