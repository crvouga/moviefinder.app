import { defineConfig, devices } from "@playwright/test";
export default defineConfig({
  testDir: "tests/e2e",
  testMatch: "*.spec.ts",
  fullyParallel: false,
  workers: 1,
  timeout: 45000,
  retries: 0,
  reporter: "list",
  use: {
    baseURL: "http://127.0.0.1:5188",
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
  },
  projects: [
    { name: "mobile-chromium", use: { ...devices["Pixel 7"], defaultBrowserType: "chromium" } },
    { name: "desktop-chromium", use: { viewport: { width: 1440, height: 1000 } } },
  ],
  webServer: {
    command: "bun tests/e2e/server.ts",
    url: "http://127.0.0.1:5188/health",
    reuseExistingServer: false,
    timeout: 30000,
  },
});
