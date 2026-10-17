FROM --platform=$BUILDPLATFORM oven/bun:1.4.2 AS bun
FROM --platform=$BUILDPLATFORM node:22.22.3-bookworm-slim AS build
COPY --from=bun /usr/local/bin/bun /usr/local/bin/bun
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile
COPY . .
RUN bun run build

# Production dependencies are portable JavaScript. Build tools run natively;
# the final Bun base is selected for the requested target architecture.
FROM --platform=$BUILDPLATFORM oven/bun:1.4.2 AS dependencies
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile --production --omit=peer --omit=optional

FROM oven/bun:1.4.2-slim AS runtime
WORKDIR /app
ENV NODE_ENV=production PORT=8080
COPY --from=dependencies --chown=bun:bun /app/node_modules ./node_modules
COPY --from=build --chown=bun:bun /app/dist ./dist
COPY --chown=bun:bun src/server ./src/server
COPY --chown=bun:bun src/shared ./src/shared
COPY --chown=bun:bun package.json ./
USER bun
EXPOSE 8080
STOPSIGNAL SIGTERM
CMD ["bun", "src/server/index.ts"]
