import { log } from "./log";
import { betterAuth } from "better-auth";
import { drizzleAdapter } from "better-auth/adapters/drizzle";
import { username } from "better-auth/plugins";
import { APIError } from "better-auth/api";
import type { Database } from "./db";
import type { Config } from "./config";
import * as schema from "./db/schema";
export function createAuth(db: Database, config: Config) {
  const mailReady = !!config.RESEND_API_KEY && !!config.EMAIL_FROM;
  return betterAuth({
    appName: "MovieFinder",
    logger: {
      level: "error",
      log: (level) => log("auth", "Authentication operation failed", { level }),
    },
    // Let Hono sanitize unexpected errors. The auth router otherwise prints SQL and parameters.
    // Expected APIErrors still become their normal HTTP responses inside Better Call.
    onAPIError: { throw: true },
    secret: config.BETTER_AUTH_SECRET,
    baseURL: config.MOVIEFINDER_ORIGIN,
    trustedOrigins: [config.MOVIEFINDER_ORIGIN],
    database: drizzleAdapter(db, {
      provider: "pg",
      schema: {
        user: schema.user,
        session: schema.session,
        account: schema.account,
        verification: schema.verification,
      },
    }),
    emailAndPassword: {
      enabled: true,
      minPasswordLength: 10,
      maxPasswordLength: 128,
      revokeSessionsOnPasswordReset: true,
      sendResetPassword: async ({ user, url }) => {
        if (!mailReady)
          throw new APIError("SERVICE_UNAVAILABLE", {
            message: "Account recovery email is not configured. Contact the service operator.",
          });
        const res = await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${config.RESEND_API_KEY}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            from: config.EMAIL_FROM,
            to: user.email,
            subject: "Reset your MovieFinder password",
            text: `Reset your password using this link: ${url}\nIf you did not request this, ignore this email.`,
          }),
          signal: AbortSignal.timeout(15000),
        });
        if (!res.ok)
          throw new APIError("SERVICE_UNAVAILABLE", {
            message: "Recovery email could not be delivered. Try again later.",
          });
      },
    },
    user: { additionalFields: { bio: { type: "string", required: false, defaultValue: "" } } },
    socialProviders:
      config.MOVIEFINDER_GOOGLE_CLIENT_ID && config.MOVIEFINDER_GOOGLE_CLIENT_SECRET
        ? {
            google: {
              clientId: config.MOVIEFINDER_GOOGLE_CLIENT_ID,
              clientSecret: config.MOVIEFINDER_GOOGLE_CLIENT_SECRET,
            },
          }
        : {},
    session: { expiresIn: 60 * 60 * 24 * 14, updateAge: 60 * 60 * 24 },
    plugins: [username()],
    advanced: {
      useSecureCookies: config.NODE_ENV === "production",
      database: { generateId: () => crypto.randomUUID() },
    },
    rateLimit: { enabled: config.NODE_ENV !== "test", window: 60, max: 60 },
  });
}
export type Auth = ReturnType<typeof createAuth>;
