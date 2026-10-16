import { and, gt, or, eq, isNull, sql, desc } from "drizzle-orm";
import type { Database, Executor } from "./db";
import { events } from "./db/schema";
import type { Entity, EventEnvelope, Kind } from "../shared/contracts";
export async function appendEvent(
  tx: Executor,
  subscription: string,
  entities: Entity[],
  removed: { kind: Kind; id: string }[] = [],
  audience: string | null = null,
  type: EventEnvelope["type"] = "upsert",
) {
  // Held until commit: ensures event IDs reflect commit order, including across processes.
  await tx.execute(sql`select pg_advisory_xact_lock(636712031)`);
  const payload: Omit<EventEnvelope, "id"> = {
    protocol: 1,
    type,
    subscription,
    entities,
    removed,
    timestamp: new Date().toISOString(),
  };
  const [row] = await tx
    .insert(events)
    .values({ subscription, payload, audience })
    .returning({ id: events.id });
  return String(row!.id);
}
export async function currentCursor(db: Executor) {
  const [row] = await db.select({ id: events.id }).from(events).orderBy(desc(events.id)).limit(1);
  return String(row?.id ?? 0);
}
export async function replay(db: Database, userId: string, cursor: number) {
  return db
    .select()
    .from(events)
    .where(and(gt(events.id, cursor), or(isNull(events.audience), eq(events.audience, userId))))
    .orderBy(events.id)
    .limit(100);
}
