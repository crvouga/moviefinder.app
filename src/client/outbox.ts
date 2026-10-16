import { commandSchema, type Command } from "../shared/contracts";
export function createOutbox(storage: Pick<Storage, "getItem" | "setItem" | "removeItem">) {
  const key = (userId: string) => `moviefinder:commands:${userId}`;
  function read(userId: string): Command[] {
    try {
      const parsed: unknown = JSON.parse(storage.getItem(key(userId)) ?? "[]");
      if (!Array.isArray(parsed)) return [];
      return parsed.slice(0, 100).flatMap((value) => {
        const result = commandSchema.safeParse(value);
        return result.success ? [result.data] : [];
      });
    } catch {
      return [];
    }
  }
  function write(userId: string, commands: Command[]) {
    if (commands.length) storage.setItem(key(userId), JSON.stringify(commands));
    else storage.removeItem(key(userId));
  }
  return {
    read,
    put(userId: string, command: Command) {
      const pending = read(userId);
      if (pending.some((c) => c.operationId === command.operationId)) return;
      if (pending.length >= 100)
        throw new Error("Your pending changes need to finish syncing before you can add more.");
      write(userId, [...pending, command]);
    },
    remove(userId: string, id: string) {
      write(
        userId,
        read(userId).filter((c) => c.operationId !== id),
      );
    },
    alias(userId: string, from: string, to: string) {
      if (from === to) return;
      write(
        userId,
        read(userId).map((c) =>
          c.type === "list.move"
            ? {
                ...c,
                itemId: c.itemId === from ? to : c.itemId,
                beforeId: c.beforeId === from ? to : c.beforeId,
              }
            : c.type === "list.remove"
              ? { ...c, itemId: c.itemId === from ? to : c.itemId }
              : c,
        ),
      );
    },
  };
}
