import { readFile, rm } from "node:fs/promises";
import { execFile } from "node:child_process";
import { promisify } from "node:util";
const pidFile = ".local/dev.pid";
const pid = Number(await readFile(pidFile, "utf8").catch(() => ""));
if (!Number.isInteger(pid) || pid <= 0) process.exit(0);
try {
  // A stale PID file must never signal an unrelated process.
  const { stdout } = await promisify(execFile)("ps", ["-p", String(pid), "-o", "command="]);
  if (!/\bbun\s+scripts\/dev\.ts\s*$/.test(stdout.trim()))
    throw new Error("Local launcher PID no longer belongs to MovieFinder; refusing to signal it");
  process.kill(pid, "SIGTERM");
  for (let attempt = 0; attempt < 80; attempt++) {
    try {
      process.kill(pid, 0);
    } catch {
      await rm(pidFile, { force: true });
      process.exit(0);
    }
    await Bun.sleep(125);
  }
  throw new Error("MovieFinder's local launcher did not stop within 10 seconds");
} catch (error) {
  if (
    error &&
    typeof error === "object" &&
    "code" in error &&
    (error.code === "ESRCH" || error.code === 1)
  )
    await rm(pidFile, { force: true });
  else throw error;
}
