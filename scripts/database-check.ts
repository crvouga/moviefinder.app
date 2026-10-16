import { createDatabase } from "../src/server/db";
import { assertDatabaseReady, databaseStartupMessage } from "../src/server/db/readiness";

const url = process.env.DATABASE_URL;
if (!url) {
  console.error(
    "DATABASE_URL is required. Use bun run db:check to load development configuration through Vault.",
  );
  process.exit(1);
}
const { db, pool } = createDatabase(url);
try {
  await assertDatabaseReady(db);
  console.log(
    "MovieFinder database tables and columns are ready. No migrations or writes performed.",
  );
} catch (error) {
  console.error(databaseStartupMessage(error));
  process.exitCode = 1;
} finally {
  await pool.end();
}
