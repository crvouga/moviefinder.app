// Approval is the merged platform inventory, not possession of DATABASE_URL.
export function verifyAllocation(document: unknown) {
  const inventory = document as {
    locals?: {
      inventory?: {
        services?: Array<{
          id?: string;
          github_repo?: string;
          database_schema?: string;
          manage_database_schema?: boolean;
        }>;
      };
    };
  };
  const service = inventory.locals?.inventory?.services?.find((s) => s.id === "moviefinder");
  if (
    service?.github_repo !== "crvouga/moviefinder" ||
    service.database_schema !== "moviefinder" ||
    !service.manage_database_schema
  )
    throw new Error(
      "Production migration blocked: crvouga/workspace main has not approved this repository’s moviefinder allocation.",
    );
}
if (import.meta.main) {
  const response = await fetch(
    "https://raw.githubusercontent.com/crvouga/workspace/main/packages/infra/tofu/modules/inventory/inventory.tf.json",
    { signal: AbortSignal.timeout(15000) },
  );
  if (!response.ok) throw new Error("Could not verify the approved platform allocation.");
  verifyAllocation(await response.json());
  process.stdout.write("Verified the merged MovieFinder schema allocation.\n");
}
