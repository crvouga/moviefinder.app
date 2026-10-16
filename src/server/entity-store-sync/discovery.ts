import { supportsGenres } from "../../shared/genres";
import { eq, sql } from "drizzle-orm";
import { z } from "zod";
import type { Database } from "../db";
import { queryCache } from "../db/schema";
import { createEntityStore } from "../entity-store";
import { appendEvent } from "../events";
import { canonicalId, type FeedParams } from "../../shared/contracts";
import { type Tmdb, upstreamPage, UpstreamError } from "./tmdb";
const bufferItem = z.object({ id: z.string(), score: z.number(), date: z.string() });
const lane = z.object({
  nextPage: z.number().int().positive(),
  ended: z.boolean(),
  buffer: z.array(bufferItem),
});
const mergeState = z.object({ movie: lane, tv: lane });
export function supportsMerge(p: FeedParams) {
  return (
    p.type === "both" &&
    !p.q &&
    ["popularity", "rating", "lowest", "newest", "oldest"].includes(p.sort)
  );
}
export async function mergeDiscovery(
  db: Database,
  tmdb: Tmdb,
  p: FeedParams,
  page: number,
  qid: string,
  parameters: (p: FeedParams, page: number, type: "movie" | "tv") => Record<string, string>,
  signal: AbortSignal,
) {
  const store = createEntityStore(db),
    base = qid.slice(0, qid.lastIndexOf(":"));
  let state: z.infer<typeof mergeState>;
  if (page === 1)
    state = {
      movie: { nextPage: 1, ended: !supportsGenres("movie", p.genres), buffer: [] },
      tv: { nextPage: 1, ended: !supportsGenres("tv", p.genres), buffer: [] },
    };
  else {
    const [previous] = await db
      .select()
      .from(queryCache)
      .where(eq(queryCache.id, `${base}:${page - 1}`));
    if (!previous?.complete) throw new UpstreamError(400);
    state = mergeState.parse(previous.params.providerState);
  }
  const ids: string[] = [];
  async function fill(type: "movie" | "tv") {
    const lane = state[type];
    if (lane.buffer.length || lane.ended) return;
    const payload = upstreamPage.parse(
      await tmdb(`discover/${type}`, parameters(p, lane.nextPage, type), signal),
    );
    await db.transaction(async (tx) => {
      for (const row of payload.results) {
        const id = canonicalId(type, row.id);
        await store.ingest(tx, id, row);
        lane.buffer.push({
          id,
          score:
            p.sort === "rating" || p.sort === "lowest"
              ? Number(row.vote_average ?? 0)
              : Number(row.popularity ?? 0),
          date:
            typeof (row.release_date ?? row.first_air_date) === "string"
              ? String(row.release_date ?? row.first_air_date)
              : "",
        });
      }
      await tx
        .update(queryCache)
        .set({
          ids: [...new Set([...ids, ...lane.buffer.map((e) => e.id)])].slice(0, 20),
          complete: false,
          fetchedAt: new Date(),
          version: sql`${queryCache.version}+1`,
        })
        .where(eq(queryCache.id, qid));
      await appendEvent(tx, qid, [], [], null, "upsert");
    });
    lane.ended = lane.nextPage >= Math.min(payload.total_pages ?? lane.nextPage, 500);
    lane.nextPage++;
  }
  const compare = (a: z.infer<typeof bufferItem>, b: z.infer<typeof bufferItem>) =>
    p.sort === "newest" || p.sort === "oldest"
      ? (p.sort === "newest" ? -1 : 1) * a.date.localeCompare(b.date) || a.id.localeCompare(b.id)
      : (p.sort === "lowest" ? 1 : -1) * (a.score - b.score) || a.id.localeCompare(b.id);
  const presented = new Set<string>();
  // Merge sorted provider streams using only their next unread buffers. A source
  // page is fetched again only when its lane runs dry, including between output pages.
  while (ids.length < 20) {
    signal.throwIfAborted();
    await fill("movie");
    await fill("tv");
    const a = state.movie.buffer[0],
      b = state.tv.buffer[0];
    if (!a && !b) break;
    const type = !b || (a && compare(a, b) <= 0) ? "movie" : "tv",
      next = state[type].buffer.shift()!;
    if (presented.has(next.id)) continue;
    presented.add(next.id);
    ids.push(next.id);
    if (ids.length % 5 === 0)
      await db.transaction(async (tx) => {
        await tx
          .update(queryCache)
          .set({
            ids: [...ids],
            complete: false,
            fetchedAt: new Date(),
            version: sql`${queryCache.version}+1`,
          })
          .where(eq(queryCache.id, qid));
        await appendEvent(tx, qid, [], [], null, "upsert");
      });
  }
  return {
    ids,
    hasNext:
      state.movie.buffer.length > 0 ||
      state.tv.buffer.length > 0 ||
      !state.movie.ended ||
      !state.tv.ended,
    providerState: state,
  };
}
