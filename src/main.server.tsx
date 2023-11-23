import cors from 'cors';
import { createHTTPServer } from '@trpc/server/adapters/standalone';
import { appRouter } from './app.server';


const adapter = createHTTPServer({
  router: appRouter,
  middleware: cors(),
  createContext() {
    return {};
  },
});

const port = Number(process.env["PORT"] ?? process.env["port"] ?? 2022);

console.log("[server] starting");

adapter.server.addListener("listening", () => {
  console.log("[server] listening");
});

adapter.listen(port);
