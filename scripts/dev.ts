// Check before starting either server so an unmigrated database cannot look usable.
const preflight = Bun.spawn(["bun", "scripts/database-check.ts"], {
  stdout: "inherit",
  stderr: "inherit",
});
const stopPreflight = () => preflight.kill();
process.once("SIGINT", stopPreflight);
process.once("SIGTERM", stopPreflight);
const preflightCode = await preflight.exited;
process.removeListener("SIGINT", stopPreflight);
process.removeListener("SIGTERM", stopPreflight);
if (preflightCode !== 0) process.exit(preflightCode);

const backend = Bun.spawn(["bun", "--watch", "src/server/index.ts"], {
  stdout: "inherit",
  stderr: "inherit",
});
const frontend = Bun.spawn(["bun", "x", "--no-install", "vp", "dev"], {
  stdout: "inherit",
  stderr: "inherit",
});
function stop() {
  backend.kill();
  frontend.kill();
}
process.on("SIGINT", stop);
process.on("SIGTERM", stop);
const code = await Promise.race([backend.exited, frontend.exited]);
stop();
process.exit(code);

export {};
