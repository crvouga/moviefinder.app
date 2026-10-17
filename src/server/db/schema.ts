import {
  pgSchema,
  text,
  timestamp,
  boolean,
  integer,
  jsonb,
  bigint,
  bigserial,
  real,
  primaryKey,
  uniqueIndex,
  index,
} from "drizzle-orm/pg-core";
import type { Entity, EventEnvelope } from "../../shared/contracts";
export const mf = pgSchema("moviefinder");
const dates = () => ({
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow(),
});
export const user = mf.table("user", {
  id: text().primaryKey(),
  name: text().notNull(),
  email: text().notNull().unique(),
  emailVerified: boolean("email_verified").notNull().default(false),
  image: text(),
  username: text().unique(),
  displayUsername: text("display_username"),
  bio: text().notNull().default(""),
  ...dates(),
});
export const session = mf.table(
  "session",
  {
    id: text().primaryKey(),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    token: text().notNull().unique(),
    ipAddress: text("ip_address"),
    userAgent: text("user_agent"),
    userId: text("user_id")
      .notNull()
      .references(() => user.id, { onDelete: "cascade" }),
    ...dates(),
  },
  (t) => [index("session_user_idx").on(t.userId)],
);
export const account = mf.table(
  "account",
  {
    id: text().primaryKey(),
    accountId: text("account_id").notNull(),
    providerId: text("provider_id").notNull(),
    userId: text("user_id")
      .notNull()
      .references(() => user.id, { onDelete: "cascade" }),
    accessToken: text("access_token"),
    refreshToken: text("refresh_token"),
    idToken: text("id_token"),
    accessTokenExpiresAt: timestamp("access_token_expires_at", { withTimezone: true }),
    refreshTokenExpiresAt: timestamp("refresh_token_expires_at", { withTimezone: true }),
    scope: text(),
    password: text(),
    ...dates(),
  },
  (t) => [index("account_user_idx").on(t.userId)],
);
export const verification = mf.table(
  "verification",
  {
    id: text().primaryKey(),
    identifier: text().notNull(),
    value: text().notNull(),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    ...dates(),
  },
  (t) => [index("verification_identifier_idx").on(t.identifier)],
);
export const sources = mf.table(
  "source_entity",
  {
    id: text().primaryKey(),
    provider: text().notNull().default("tmdb"),
    entityType: text("entity_type").notNull(),
    externalId: text("external_id").notNull(),
    payload: jsonb().$type<Record<string, unknown>>().notNull(),
    schemaVersion: integer("schema_version").notNull().default(1),
    projection: text().notNull().default("summary"),
    checksum: text().notNull(),
    fetchedAt: timestamp("fetched_at", { withTimezone: true }).notNull(),
    detailFetchedAt: timestamp("detail_fetched_at", { withTimezone: true }),
    refreshAt: timestamp("refresh_at", { withTimezone: true }).notNull(),
    lastSuccess: timestamp("last_success", { withTimezone: true }).notNull(),
    lastFailure: timestamp("last_failure", { withTimezone: true }),
    error: text(),
    version: bigint({ mode: "number" }).notNull().default(1),
    title: text().notNull(),
    genres: integer().array().notNull().default([]),
    popularity: real().notNull().default(0),
    rating: real().notNull().default(0),
    releaseDate: text("release_date"),
  },
  (t) => [
    uniqueIndex("source_identity_idx").on(t.provider, t.entityType, t.externalId),
    index("source_type_popularity_idx").on(t.entityType, t.popularity),
    index("source_type_rating_idx").on(t.entityType, t.rating),
    index("source_release_idx").on(t.releaseDate),
    index("source_refresh_idx").on(t.refreshAt),
  ],
);
export const queryCache = mf.table("query_cache", {
  id: text().primaryKey(),
  name: text().notNull(),
  params: jsonb().$type<Record<string, unknown>>().notNull(),
  ids: text().array().notNull().default([]),
  complete: boolean().notNull().default(false),
  nextPage: integer("next_page"),
  fetchedAt: timestamp("fetched_at", { withTimezone: true }),
  refreshAt: timestamp("refresh_at", { withTimezone: true }),
  error: text(),
  version: bigint({ mode: "number" }).notNull().default(1),
});
export const jobs = mf.table(
  "sync_job",
  {
    id: text().primaryKey(),
    kind: text().notNull(),
    params: jsonb().$type<Record<string, unknown>>().notNull(),
    state: text().notNull().default("pending"),
    attempts: integer().notNull().default(0),
    runAt: timestamp("run_at", { withTimezone: true }).notNull().defaultNow(),
    leaseUntil: timestamp("lease_until", { withTimezone: true }),
    error: text(),
    ...dates(),
  },
  (t) => [index("sync_job_due_idx").on(t.state, t.runAt)],
);
export const checkpoints = mf.table("sync_checkpoint", {
  id: text().primaryKey(),
  payload: jsonb().$type<{ resumeAt: number }>().notNull(),
  updatedAt: timestamp("updated_at", { withTimezone: true }).notNull().defaultNow(),
});
export const statuses = mf.table(
  "title_status",
  {
    id: text().primaryKey(),
    userId: text("user_id")
      .notNull()
      .references(() => user.id, { onDelete: "cascade" }),
    titleId: text("title_id")
      .notNull()
      .references(() => sources.id),
    seen: boolean().notNull().default(false),
    sentiment: text().notNull().default("unrated"),
    version: bigint({ mode: "number" }).notNull().default(1),
    ...dates(),
  },
  (t) => [uniqueIndex("status_user_title_idx").on(t.userId, t.titleId)],
);
export const lists = mf.table("list", {
  id: text().primaryKey(),
  ownerId: text("owner_id")
    .notNull()
    .references(() => user.id),
  title: text().notNull(),
  description: text().notNull().default(""),
  version: bigint({ mode: "number" }).notNull().default(1),
  ...dates(),
});
export const items = mf.table(
  "list_item",
  {
    id: text().primaryKey(),
    listId: text("list_id")
      .notNull()
      .references(() => lists.id, { onDelete: "cascade" }),
    titleId: text("title_id")
      .notNull()
      .references(() => sources.id),
    orderKey: text("order_key").notNull(),
    addedBy: text("added_by")
      .notNull()
      .references(() => user.id),
    version: bigint({ mode: "number" }).notNull().default(1),
    ...dates(),
  },
  (t) => [
    uniqueIndex("list_title_idx").on(t.listId, t.titleId),
    index("list_order_idx").on(t.listId, t.orderKey),
  ],
);
export const collaborators = mf.table(
  "collaborator",
  {
    id: text().primaryKey(),
    listId: text("list_id")
      .notNull()
      .references(() => lists.id, { onDelete: "cascade" }),
    userId: text("user_id")
      .notNull()
      .references(() => user.id),
    role: text().notNull(),
    version: bigint({ mode: "number" }).notNull().default(1),
  },
  (t) => [uniqueIndex("collaborator_list_user_idx").on(t.listId, t.userId)],
);
export const invitations = mf.table(
  "invitation",
  {
    id: text().primaryKey(),
    listId: text("list_id")
      .notNull()
      .references(() => lists.id, { onDelete: "cascade" }),
    userId: text("user_id")
      .notNull()
      .references(() => user.id),
    invitedBy: text("invited_by")
      .notNull()
      .references(() => user.id),
    state: text().notNull().default("pending"),
    version: bigint({ mode: "number" }).notNull().default(1),
    ...dates(),
  },
  (t) => [uniqueIndex("invitation_list_user_idx").on(t.listId, t.userId)],
);
export const follows = mf.table(
  "follow",
  {
    id: text().primaryKey(),
    userId: text("user_id")
      .notNull()
      .references(() => user.id),
    targetId: text("target_id").notNull(),
    targetKind: text("target_kind").notNull(),
    version: bigint({ mode: "number" }).notNull().default(1),
  },
  (t) => [uniqueIndex("follow_user_target_idx").on(t.userId, t.targetId, t.targetKind)],
);
export const feeds = mf.table("saved_feed", {
  id: text().primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id),
  title: text().notNull(),
  params: jsonb().$type<Record<string, unknown>>().notNull(),
  version: bigint({ mode: "number" }).notNull().default(1),
});
export const activities = mf.table("activity", {
  id: text().primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id),
  title: text().notNull(),
  targetId: text("target_id").notNull(),
  version: bigint({ mode: "number" }).notNull().default(1),
  ...dates(),
});
export const events = mf.table(
  "event",
  {
    id: bigserial({ mode: "number" }).primaryKey(),
    audience: text(),
    subscription: text().notNull(),
    payload: jsonb().$type<Omit<EventEnvelope, "id">>().notNull(),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [index("event_created_idx").on(t.createdAt)],
);
export const operations = mf.table(
  "operation",
  {
    userId: text("user_id")
      .notNull()
      .references(() => user.id, { onDelete: "cascade" }),
    id: text().notNull(),
    fingerprint: text().notNull(),
    result: jsonb()
      .$type<{ entities: Entity[]; removed: { kind: Entity["kind"]; id: string }[] }>()
      .notNull(),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [primaryKey({ columns: [t.userId, t.id] })],
);
