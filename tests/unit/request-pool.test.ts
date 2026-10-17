import { describe, expect, it, vi } from "vite-plus/test";
import { RequestPool } from "../../src/client/request-pool";
function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((yes) => {
    resolve = yes;
  });
  return { promise, resolve };
}
describe("shared navigation requests", () => {
  it("keeps a request alive when navigation adopts abandoned speculative work", async () => {
    const pool = new RequestPool<string>(1);
    const pending = deferred<string>();
    const speculative = new AbortController();
    let shared!: AbortSignal;
    const load = vi.fn((signal: AbortSignal) => {
      shared = signal;
      return pending.promise;
    });
    const first = pool.run("title", load, { signal: speculative.signal, speculative: true });
    const navigation = pool.run("title", load);
    speculative.abort();
    await expect(first).rejects.toBeDefined();
    expect(shared.aborted).toBe(false);
    pending.resolve("entity:title");
    await expect(navigation).resolves.toBe("entity:title");
    expect(load).toHaveBeenCalledTimes(1);
    await expect(pool.run("title", load)).resolves.toBe("entity:title");
    expect(load).toHaveBeenCalledTimes(1);
  });
  it("cancels unused requests and never starts abandoned queued work", async () => {
    const pool = new RequestPool<string>(1);
    const pending = deferred<string>();
    const first = new AbortController();
    const queued = new AbortController();
    let shared!: AbortSignal;
    const a = pool.run(
      "a",
      (signal) => {
        shared = signal;
        return pending.promise;
      },
      { signal: first.signal, speculative: true },
    );
    const load = vi.fn(async () => "b");
    const b = pool.run("b", load, { signal: queued.signal, speculative: true });
    queued.abort();
    first.abort();
    await expect(b).rejects.toBeDefined();
    await expect(a).rejects.toBeDefined();
    expect(shared.aborted).toBe(true);
    pending.resolve("a");
    await Promise.resolve();
    expect(load).not.toHaveBeenCalled();
  });
  it("promotes a queued prediction without waiting for unrelated work", async () => {
    const pool = new RequestPool<string>(1);
    const pending = deferred<string>();
    const a = pool.run("a", () => pending.promise, { speculative: true });
    const load = vi.fn(async () => "b");
    const b = pool.run("b", load, { speculative: true });
    expect(load).not.toHaveBeenCalled();
    await expect(pool.run("b", load)).resolves.toBe("b");
    await expect(b).resolves.toBe("b");
    expect(load).toHaveBeenCalledTimes(1);
    pending.resolve("a");
    await a;
  });
  it("retries failed and stale reads, invalidates on durable updates", async () => {
    const pool = new RequestPool<string>();
    await expect(
      pool.run("a", async () => {
        throw new Error("offline");
      }),
    ).rejects.toThrow("offline");
    const load = vi.fn(async () => "a");
    await pool.run("a", load, { ttl: () => 0 });
    await pool.run("a", load);
    expect(load).toHaveBeenCalledTimes(2);
    pool.invalidate();
    await pool.run("a", load);
    expect(load).toHaveBeenCalledTimes(3);
  });
});
