import { mkdir, writeFile, mkdtemp, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { openLocalDatabase, openAtlasScratch, migrateLocalDatabase } from "./local-database";
const name = process.argv[2] ?? "change",
  checkOnly = process.argv.includes("--check");
if (!/^[a-z][a-z0-9_]*$/.test(name)) {
  console.error("Use a lowercase migration name with underscores");
  process.exit(1);
}
let database: Awaited<ReturnType<typeof openLocalDatabase>> | undefined;
let scratch: Awaited<ReturnType<typeof openAtlasScratch>> | undefined;
let directory: string | undefined;
let exitCode = 0;
async function run(args: string[], capture = false, env = process.env) {
  const child = Bun.spawn(args, { stdout: capture ? "pipe" : "inherit", stderr: "inherit", env });
  const output = capture ? await new Response(child.stdout).text() : "";
  const code = await child.exited;
  if (code !== 0) throw new Error(`${args[0]} failed (${code})`);
  return output;
}
try {
  directory = await mkdtemp(join(tmpdir(), "moviefinder-schema-"));
  database = await openLocalDatabase(directory);
  const url = database.url;
  await migrateLocalDatabase(url);
  scratch = await openAtlasScratch(url);
  const output = await run(
    [
      "atlas",
      "schema",
      "diff",
      "--env",
      "app",
      "--from",
      "env://url",
      "--to",
      "file://atlas/schema.sql",
      "--dev-url",
      scratch.url,
    ],
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
} catch (error) {
  console.error(error instanceof Error ? error.message : "Atlas schema verification failed");
  exitCode = 1;
} finally {
  await scratch?.stop();
  await database?.stop();
  if (directory) await rm(directory, { recursive: true, force: true });
}
process.exit(exitCode);
