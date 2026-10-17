import { openLocalDatabase, migrateLocalDatabase } from "./local-database";
import { createDatabase } from "../src/server/db";
import { assertDatabaseReady } from "../src/server/db/readiness";
import { z } from "zod";
const action = z.enum(["apply", "status", "check", "drift"]).parse(process.argv[2]);
let database: Awaited<ReturnType<typeof openLocalDatabase>> | undefined;
let exitCode = 0;
try {
  database = await openLocalDatabase();
  if (action === "check") {
    const { db, pool } = createDatabase(database.url);
    try {
      await assertDatabaseReady(db);
      console.log("Local MovieFinder tables and columns are ready.");
    } finally {
      await pool.end();
    }
  } else await migrateLocalDatabase(database.url, action);
} catch {
  console.error(
    "Local database is not ready. Run bun run dev to apply local Atlas migrations automatically. Shared database allocation is not required for local development.",
  );
  exitCode = 1;
} finally {
  await database?.stop();
}
// embedded-postgres's exit hook otherwise replaces process.exitCode with zero.
process.exit(exitCode);
