import { mkdir, writeFile } from "node:fs/promises";
import { createDatabase } from "../src/server/db";
import { sql } from "drizzle-orm";
const name = process.argv[2] ?? "change",
  checkOnly = process.argv.includes("--check");
if (!/^[a-z][a-z0-9_]*$/.test(name))
  throw new Error("Use a lowercase migration name with underscores");
const container = `moviefinder-schema-${process.pid}`;
let started = false;
async function run(args: string[], capture = false, env = process.env) {
  const child = Bun.spawn(args, { stdout: capture ? "pipe" : "inherit", stderr: "inherit", env });
  const output = capture ? await new Response(child.stdout).text() : "";
  const code = await child.exited;
  if (code !== 0) throw new Error(`${args[0]} failed (${code})`);
  return output;
}
try {
  await run([
    "docker",
    "run",
    "--rm",
    "-d",
    "--name",
    container,
    "-e",
    "POSTGRES_PASSWORD=local-schema-only",
    "-e",
    "POSTGRES_DB=moviefinder_test",
    "-p",
    "127.0.0.1::5432",
    "postgres:17.6-alpine",
  ]);
  started = true;
  const port = (await run(["docker", "port", container, "5432/tcp"], true))
      .trim()
      .split(":")
      .at(-1),
    url = `postgres://postgres:local-schema-only@127.0.0.1:${port}/moviefinder_test?sslmode=disable`,
    { db, pool } = createDatabase(url);
  try {
    let ready = false;
    for (let i = 0; i < 90; i++) {
      try {
        await db.execute(sql`select 1`);
        ready = true;
        break;
      } catch {
        await Bun.sleep(500);
      }
    }
    if (!ready) throw new Error("Disposable schema database unavailable");
    await db.execute(sql`create schema moviefinder`);
  } finally {
    await pool.end();
  }
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
  const output = await run(
    ["atlas", "schema", "diff", "--env", "app", "--from", url, "--to", "file://atlas/schema.sql"],
    true,
    { ...process.env, DATABASE_URL: url },
  );
  if (output.includes("Schemas are synced")) console.log("No schema changes.");
  else {
    if (checkOnly) throw new Error("Committed Atlas migrations differ from atlas/schema.sql");
    if (!output.trim() || !/(ALTER|CREATE|DROP)/.test(output))
      throw new Error("Atlas returned no valid migration");
    if (/DROP\s+(TABLE|SCHEMA)|DROP\s+COLUMN/i.test(output))
      throw new Error(
        "Destructive migration generation requires a reviewed expand/migrate/contract change; inspect atlas schema diff manually.",
      );
    const timestamp = new Date().toISOString().replace(/\D/g, "").slice(0, 14);
    await mkdir("atlas/migrations", { recursive: true });
    const path = `atlas/migrations/${timestamp}_${name}.sql`;
    await writeFile(path, output, { flag: "wx" });
    await run(["atlas", "migrate", "hash", "--dir", "file://atlas/migrations"]);
    console.log(`Atlas generated ${path}`);
  }
} finally {
  if (started) await run(["docker", "stop", container]);
}
