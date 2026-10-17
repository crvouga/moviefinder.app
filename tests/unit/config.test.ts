import { expect, it } from "vite-plus/test";

import { readConfig } from "../../src/server/config";
it("uses the project-owned auth secret while preserving existing local configuration", () => {
  const legacy = "local-development-only-secret-long-enough";
  const project = "project-owned-production-secret-long-enough";
  expect(readConfig({ DATABASE_URL: "test", BETTER_AUTH_SECRET: legacy }).BETTER_AUTH_SECRET).toBe(
    legacy,
  );
  expect(
    readConfig({
      DATABASE_URL: "test",
      BETTER_AUTH_SECRET: legacy,
      MOVIEFINDER_AUTH_SECRET: project,
    }).BETTER_AUTH_SECRET,
  ).toBe(project);
  expect(() =>
    readConfig({
      DATABASE_URL: "test",
      BETTER_AUTH_SECRET: legacy,
      MOVIEFINDER_AUTH_SECRET: "invalid",
    }),
  ).toThrow("Invalid configuration");
});

import { verifyAllocation } from "../../scripts/verify-allocation";
it("requires the merged platform ownership record before production migrations", () => {
  const entry = {
    id: "moviefinder",
    github_repo: "crvouga/moviefinder",
    database_schema: "moviefinder",
    manage_database_schema: true,
  };
  const inventory = (service: typeof entry) => ({ locals: { inventory: { services: [service] } } });
  expect(() => verifyAllocation(inventory(entry))).not.toThrow();
  expect(() => verifyAllocation({})).toThrow("has not approved");
  expect(() =>
    verifyAllocation(inventory({ ...entry, github_repo: "crvouga/moviefinder.app-rust" })),
  ).toThrow("has not approved");
  expect(() => verifyAllocation(inventory({ ...entry, database_schema: "public" }))).toThrow(
    "has not approved",
  );
});
