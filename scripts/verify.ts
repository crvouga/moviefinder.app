import { createDatabase } from "../src/server/db";
import { sql } from "drizzle-orm";
const mode = process.argv[2] ?? "integration";
const container = `moviefinder-test-${process.pid}`;
let started = false;
async function run(args: string[], env: Record<string, string | undefined> = process.env) {
  const child = Bun.spawn(args, { stdout: "inherit", stderr: "inherit", env });
  const code = await child.exited;
  if (code !== 0) throw new Error(`${args[0]} failed with exit code ${code}`);
}
try {
  let url = process.env.TEST_DATABASE_URL;
  if (!url) {
    await run([
      "docker",
      "run",
      "--rm",
      "-d",
      "--name",
      container,
      "-e",
      "POSTGRES_PASSWORD=local-test-only",
      "-e",
      "POSTGRES_DB=moviefinder_test",
      "-p",
      "127.0.0.1::5432",
      "postgres:17.6-alpine",
    ]);
    started = true;
    const inspect = Bun.spawn(["docker", "port", container, "5432/tcp"], { stdout: "pipe" });
    const port = (await new Response(inspect.stdout).text()).trim().split(":").at(-1);
    url = `postgres://postgres:local-test-only@127.0.0.1:${port}/moviefinder_test?sslmode=disable`;
  }
  const parsed = new URL(url);
  if (!["127.0.0.1", "localhost"].includes(parsed.hostname) || !parsed.pathname.endsWith("_test"))
    throw new Error("Refusing integration tests against a non-disposable database");
  const { db, pool } = createDatabase(url);
  let ready = false;
  for (let attempt = 0; attempt < 90; attempt++) {
    try {
      await db.execute(sql`select 1`);
      ready = true;
      break;
    } catch {
      await Bun.sleep(500);
    }
  }
  if (!ready) throw new Error("Test PostgreSQL did not become ready");
  await db.execute(sql`create schema if not exists moviefinder`);
  await pool.end();
  await run([
    "atlas",
    "migrate",
    "apply",
    "--url",
    url,
    "--revisions-schema",
    "moviefinder",
    "--dir",
    "file://atlas/migrations",
  ]);
  await run(
    mode === "e2e"
      ? ["bun", "x", "--no-install", "playwright", "test"]
      : ["bun", "x", "--no-install", "vp", "test", "run", "--project", "integration"],
    { ...process.env, TEST_DATABASE_URL: url },
  );
} finally {
  if (started) await run(["docker", "stop", container]);
}
