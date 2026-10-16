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
