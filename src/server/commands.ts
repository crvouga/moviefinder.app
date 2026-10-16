import { normalize } from "./entity-store/normalize";
import { and, eq, inArray, sql } from "drizzle-orm";
import { generateKeyBetween } from "fractional-indexing";
import type { Database } from "./db";
import * as s from "./db/schema";
import {
  type Command,
  type Entity,
  type Kind,
  stableSerialize,
  canEdit,
  canManage,
} from "../shared/contracts";
import { digest } from "./entity-store";
import { rowEntity } from "./state";
import { appendEvent } from "./events";
import { AppError } from "./log";
export async function executeCommand(db: Database, userId: string, command: Command) {
  return db.transaction(async (tx) => {
    // Global commit order lock also makes duplicated operation IDs safe across replicas.
    await tx.execute(sql`select pg_advisory_xact_lock(636712031)`);
    const fingerprint = digest(stableSerialize(command));
    const [previous] = await tx
      .select()
      .from(s.operations)
      .where(and(eq(s.operations.userId, userId), eq(s.operations.id, command.operationId)));
    if (previous) {
      if (previous.fingerprint !== fingerprint)
        throw new AppError(409, "Operation ID already belongs to another command");
      return previous.result;
    }
    const entities: Entity[] = [],
      removed: { kind: Kind; id: string }[] = [];
    let audience: string | null = null;
    let subscription = "social";
    const push = (kind: Kind, rows: Record<string, unknown>[]) =>
      entities.push(...rows.map((row) => rowEntity(kind, row)));
    const role = async (listId: string, owner = false) => {
      const [list] = await tx.select().from(s.lists).where(eq(s.lists.id, listId));
      if (!list) throw new AppError(404, "List not found");
      const [member] = await tx
        .select()
        .from(s.collaborators)
        .where(and(eq(s.collaborators.listId, listId), eq(s.collaborators.userId, userId)));
      if (!(owner ? canManage(member?.role) : canEdit(member?.role)))
        throw new AppError(
          403,
          owner
            ? "Only the list owner can manage this list"
            : "Join this list as an editor to make changes",
        );
      subscription = `list:${listId}`;
      return list;
    };
    const titleExists = async (titleId: string) => {
      const [source] = await tx
        .select({ id: s.sources.id })
        .from(s.sources)
        .where(eq(s.sources.id, titleId));
      if (!source) throw new AppError(409, "Title details must be cached before this action");
    };
    const activity = async (title: string, targetId: string) =>
      push(
        "activity",
        await tx
          .insert(s.activities)
          .values({ id: crypto.randomUUID(), userId, title, targetId })
          .returning(),
      );
    switch (command.type) {
      case "status": {
        await titleExists(command.titleId);
        const id = `${userId}:${command.titleId}`;
        const patch = {
          ...(command.seen === undefined ? {} : { seen: command.seen }),
          ...(command.sentiment === undefined ? {} : { sentiment: command.sentiment }),
        };
        push(
          "status",
          await tx
            .insert(s.statuses)
            .values({ id, userId, titleId: command.titleId, ...patch })
            .onConflictDoUpdate({
              target: [s.statuses.userId, s.statuses.titleId],
              set: { ...patch, version: sql`${s.statuses.version}+1`, updatedAt: new Date() },
            })
            .returning(),
        );
        await activity("updated their take", command.titleId);
        break;
      }
      case "list.create": {
        const id = command.operationId;
        push(
          "list",
          await tx
            .insert(s.lists)
            .values({ id, ownerId: userId, title: command.title, description: command.description })
            .returning(),
        );
        push(
          "collaborator",
          await tx
            .insert(s.collaborators)
            .values({ id: crypto.randomUUID(), listId: id, userId, role: "owner" })
            .returning(),
        );
        await activity("created a list", id);
        break;
      }
      case "list.edit":
        await role(command.listId, true);
        push(
          "list",
          await tx
            .update(s.lists)
            .set({
              title: command.title,
              description: command.description,
              updatedAt: new Date(),
              version: sql`${s.lists.version}+1`,
            })
            .where(eq(s.lists.id, command.listId))
            .returning(),
        );
        break;
      case "list.add": {
        await role(command.listId);
        await titleExists(command.titleId);
        const [last] = await tx
          .select()
          .from(s.items)
          .where(eq(s.items.listId, command.listId))
          .orderBy(sql`${s.items.orderKey} collate "C" desc`)
          .limit(1);
        push(
          "item",
          await tx
            .insert(s.items)
            .values({
              id: command.operationId,
              listId: command.listId,
              titleId: command.titleId,
              addedBy: userId,
              orderKey: generateKeyBetween(last?.orderKey ?? null, null),
            })
            .onConflictDoUpdate({
              target: [s.items.listId, s.items.titleId],
              set: { titleId: command.titleId },
            })
            .returning(),
        );
        await activity("added a title to a list", command.listId);
        break;
      }
      case "list.remove": {
        await role(command.listId);
        const rows = await tx
          .delete(s.items)
          .where(and(eq(s.items.id, command.itemId), eq(s.items.listId, command.listId)))
          .returning();
        removed.push(...rows.map((r) => ({ kind: "item" as const, id: r.id })));
        break;
      }
      case "list.move": {
        await role(command.listId);
        const rows = await tx
          .select()
          .from(s.items)
          .where(eq(s.items.listId, command.listId))
          .orderBy(sql`${s.items.orderKey} collate "C"`, s.items.id);
        const item = rows.find((r) => r.id === command.itemId);
        if (!item) throw new AppError(404, "List entry was removed");
        if (command.beforeId === item.id) break;
        const rest = rows.filter((r) => r.id !== item.id);
        let index = command.beforeId
          ? rest.findIndex((r) => r.id === command.beforeId)
          : rest.length;
        if (index < 0) index = rest.length;
        const orderKey = generateKeyBetween(
          rest[index - 1]?.orderKey ?? null,
          rest[index]?.orderKey ?? null,
        );
        push(
          "item",
          await tx
            .update(s.items)
            .set({ orderKey, version: sql`${s.items.version}+1`, updatedAt: new Date() })
            .where(eq(s.items.id, item.id))
            .returning(),
        );
        break;
      }
      case "list.invite": {
        await role(command.listId, true);
        const [target] = await tx
          .select({ id: s.user.id })
          .from(s.user)
          .where(eq(s.user.id, command.userId));
        if (!target) throw new AppError(404, "User not found");
        if (command.userId === userId) throw new AppError(409, "You already own this list");
        push(
          "invitation",
          await tx
            .insert(s.invitations)
            .values({
              id: crypto.randomUUID(),
              listId: command.listId,
              userId: command.userId,
              invitedBy: userId,
            })
            .onConflictDoUpdate({
              target: [s.invitations.listId, s.invitations.userId],
              set: { state: "pending", version: sql`${s.invitations.version}+1` },
            })
            .returning(),
        );
        break;
      }
      case "invitation.accept":
      case "invitation.decline": {
        const [inv] = await tx
          .select()
          .from(s.invitations)
          .where(eq(s.invitations.id, command.invitationId));
        if (!inv || inv.userId !== userId)
          throw new AppError(403, "This invitation belongs to another user");
        if (inv.state !== "pending")
          throw new AppError(409, "Invitation has already been answered");
        if (command.type === "invitation.accept")
          push(
            "collaborator",
            await tx
              .insert(s.collaborators)
              .values({ id: crypto.randomUUID(), listId: inv.listId, userId, role: "editor" })
              .onConflictDoNothing()
              .returning(),
          );
        push(
          "invitation",
          await tx
            .update(s.invitations)
            .set({
              state: command.type === "invitation.accept" ? "accepted" : "declined",
              version: sql`${s.invitations.version}+1`,
            })
            .where(eq(s.invitations.id, inv.id))
            .returning(),
        );
        break;
      }
      case "list.member.remove": {
        await role(command.listId, true);
        const [list] = await tx.select().from(s.lists).where(eq(s.lists.id, command.listId));
        if (list?.ownerId === command.userId)
          throw new AppError(409, "The list owner cannot be removed");
        const rows = await tx
          .delete(s.collaborators)
          .where(
            and(
              eq(s.collaborators.listId, command.listId),
              eq(s.collaborators.userId, command.userId),
            ),
          )
          .returning();
        removed.push(...rows.map((r) => ({ kind: "collaborator" as const, id: r.id })));
        push(
          "invitation",
          await tx
            .update(s.invitations)
            .set({
              state: "declined",
              version: sql`${s.invitations.version}+1`,
              updatedAt: new Date(),
            })
            .where(
              and(
                eq(s.invitations.listId, command.listId),
                eq(s.invitations.userId, command.userId),
                eq(s.invitations.state, "pending"),
              ),
            )
            .returning(),
        );
        break;
      }
      case "list.follow":
      case "user.follow": {
        audience = userId;
        const targetId = command.type === "list.follow" ? command.listId : command.userId,
          targetKind = command.type === "list.follow" ? "list" : "user";
        const exists =
          command.type === "list.follow"
            ? await tx.select({ id: s.lists.id }).from(s.lists).where(eq(s.lists.id, targetId))
            : await tx.select({ id: s.user.id }).from(s.user).where(eq(s.user.id, targetId));
        if (!exists.length) throw new AppError(404, "Follow target not found");
        if (command.follow)
          push(
            "follow",
            await tx
              .insert(s.follows)
              .values({ id: crypto.randomUUID(), userId, targetId, targetKind })
              .onConflictDoNothing()
              .returning(),
          );
        else {
          const rows = await tx
            .delete(s.follows)
            .where(
              and(
                eq(s.follows.userId, userId),
                eq(s.follows.targetId, targetId),
                eq(s.follows.targetKind, targetKind),
              ),
            )
            .returning();
          removed.push(...rows.map((r) => ({ kind: "follow" as const, id: r.id })));
        }
        break;
      }
      case "list.copy": {
        const [original] = await tx.select().from(s.lists).where(eq(s.lists.id, command.listId));
        if (!original) throw new AppError(404, "List not found");
        const id = command.operationId;
        push(
          "list",
          await tx
            .insert(s.lists)
            .values({
              id,
              ownerId: userId,
              title: `${original.title} (copy)`,
              description: original.description,
            })
            .returning(),
        );
        push(
          "collaborator",
          await tx
            .insert(s.collaborators)
            .values({ id: crypto.randomUUID(), listId: id, userId, role: "owner" })
            .returning(),
        );
        const entries = await tx.select().from(s.items).where(eq(s.items.listId, original.id));
        if (entries.length)
          push(
            "item",
            await tx
              .insert(s.items)
              .values(
                entries.map((r) => ({
                  id: crypto.randomUUID(),
                  listId: id,
                  titleId: r.titleId,
                  orderKey: r.orderKey,
                  addedBy: userId,
                })),
              )
              .returning(),
          );
        break;
      }
      case "feed.save":
        audience = userId;
        push(
          "feed",
          await tx
            .insert(s.feeds)
            .values({
              id: command.operationId,
              userId,
              title: command.title,
              params: JSON.parse(stableSerialize(command.params)),
            })
            .returning(),
        );
        break;
      case "profile":
        push(
          "user",
          await tx
            .update(s.user)
            .set({
              name: command.name,
              username: command.username.toLowerCase(),
              displayUsername: command.username,
              bio: command.bio,
              image: command.image || null,
              updatedAt: new Date(),
            })
            .where(eq(s.user.id, userId))
            .returning({
              id: s.user.id,
              name: s.user.name,
              username: s.user.username,
              image: s.user.image,
              bio: s.user.bio,
              updatedAt: s.user.updatedAt,
            }),
        );
        break;
    }
    if (
      "listId" in command &&
      (entities.length > 0 || removed.length > 0) &&
      ["list.edit", "list.move", "list.remove", "list.invite", "list.member.remove"].includes(
        command.type,
      )
    ) {
      const verbs: Record<string, string> = {
        "list.edit": "updated a collection",
        "list.move": "reordered a collection",
        "list.remove": "removed a title from a collection",
        "list.invite": "invited a collaborator",
        "list.member.remove": "updated the collaborators",
      };
      await activity(verbs[command.type]!, command.listId);
    }
    // A list/status delta carries the referenced catalog projection, so a viewer
    // who has never opened that title can render it without an extra round trip.
    const referenced = [
      ...new Set(
        entities
          .filter((e) => e.kind === "item" || e.kind === "status")
          .map((e) => String(e.data.titleId)),
      ),
    ];
    if (referenced.length) {
      const records = await tx.select().from(s.sources).where(inArray(s.sources.id, referenced));
      for (const row of records) entities.push(...normalize(row.id, row.payload, row.version));
    }
    const result = { entities, removed };
    await appendEvent(tx, subscription, entities, removed, audience);
    await tx.insert(s.operations).values({ userId, id: command.operationId, fingerprint, result });
    return result;
  });
}
