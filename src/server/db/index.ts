import pg from "pg";
import { drizzle } from "drizzle-orm/node-postgres";
import * as schema from "./schema";
export function createDatabase(connectionString: string) {
  const pool = new pg.Pool({
    connectionString,
    max: 5,
    connectionTimeoutMillis: 10000,
    statement_timeout: 30000,
    idleTimeoutMillis: 30000,
  });
  pool.on("error", () =>
    console.error(
      JSON.stringify({
        level: "error",
        category: "database",
        message: "Idle PostgreSQL connection failed",
      }),
    ),
  );
  return { db: drizzle(pool, { schema }), pool };
}
export type Database = ReturnType<typeof createDatabase>["db"];
export type Transaction = Parameters<Parameters<Database["transaction"]>[0]>[0];
export type Executor = Database | Transaction;
