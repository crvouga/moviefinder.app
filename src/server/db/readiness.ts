import { is, sql } from "drizzle-orm";
import { PgTable, getTableConfig } from "drizzle-orm/pg-core";
import type { Executor } from ".";
import * as schema from "./schema";

export type DatabaseFailure = "schema" | "permissions" | "connection";

// Drizzle wraps driver errors in cause. Never log the wrapper: it holds SQL parameters.
export function databaseFailure(error: unknown): DatabaseFailure | undefined {
  let current = error;
  for (let depth = 0; depth < 8 && current && typeof current === "object"; depth++) {
    const code = "code" in current ? current.code : undefined;
    if (["42P01", "42703", "3F000"].includes(String(code))) return "schema";
    if (["42501", "28P01", "28000"].includes(String(code))) return "permissions";
    if (
      (typeof code === "string" && code.startsWith("08")) ||
      [
        "57P01",
        "57P02",
        "57P03",
        "53300",
        "ECONNREFUSED",
        "ECONNRESET",
        "ETIMEDOUT",
        "ENOTFOUND",
        "EAI_AGAIN",
      ].includes(String(code))
    )
      return "connection";
    if (
      current instanceof Error &&
      [
        "Connection terminated unexpectedly",
        "Connection terminated due to connection timeout",
        "timeout exceeded when trying to connect",
        "Connection terminated",
      ].includes(current.message)
    )
      return "connection";
    current = "cause" in current ? current.cause : undefined;
  }
}

export class DatabaseNotReady extends Error {
  constructor(public readonly missing: string[]) {
    super(
      `MovieFinder database is not initialized or is behind its migrations. Missing ${missing[0]}${missing.length > 1 ? ` (and ${missing.length - 1} other required fields)` : ""}.`,
    );
  }
}

// Read only the application's catalog metadata; never create schemas or apply DDL here.
export async function assertDatabaseReady(db: Executor) {
  const result = await db.execute<{ table_name: string; column_name: string }>(sql`
    select table_name, column_name from information_schema.columns
    where table_schema = 'moviefinder'
  `);
  const present = new Set(result.rows.map((row) => `${row.table_name}.${row.column_name}`));
  const expected = Object.values(schema)
    .filter((value) => is(value, PgTable))
    .flatMap((table) => {
      const config = getTableConfig(table);
      return config.columns.map((column) => `${config.name}.${column.name}`);
    });
  const missing = expected.filter((column) => !present.has(column));
  if (missing.length) throw new DatabaseNotReady(missing.map((column) => `moviefinder.${column}`));
}

export function databaseStartupMessage(error: unknown) {
  if (error instanceof DatabaseNotReady || databaseFailure(error) === "schema")
    return `${error instanceof DatabaseNotReady ? error.message : "MovieFinder database migrations have not been applied."}\nFirst obtain the approved moviefinder schema allocation through crvouga/workspace. After allocation approval, run bun run db:migrate, then restart MovieFinder. See docs/infrastructure.md. Startup checks never apply migrations.`;
  if (databaseFailure(error) === "permissions")
    return "MovieFinder cannot access its allocated database. Check the development DATABASE_URL and schema grants through crvouga/workspace. Connection details have been omitted.";
  return "MovieFinder could not connect to its database. Check development Vault configuration and PostgreSQL availability, then retry. Connection details have been omitted.";
}
