import 'isomorphic-fetch'
import Fastify from 'fastify';
import fastifyStatic from '@fastify/static';
import { initServer } from '@ts-rest/fastify';
import { appRouter } from './app.server';
import cors from '@fastify/cors'
import path from 'node:path'
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 
// 
// Server
// 
// 

const fastify = Fastify();

// 
// 
// CORS
// 
// 

fastify.register(cors, { 
  origin: true,
  credentials: true,
})

// 
// 
// API
// 
// 

const s = initServer()

const router = appRouter({ s })

fastify.register(s.plugin(router.auth));
fastify.register(s.plugin(router.feed));


// 
// 
// Static files
// 
// 

const rootPath = path.join(__dirname, "..", "dist")

fastify.register(fastifyStatic, {
  root: rootPath,
  index: "index.html",
});

// 
// 
// Main
// 
// 

const PORT = process.env.PORT || 8000;
const HOST = "0.0.0.0"
const main = async () => {
  console.log("starting server")
  try {
    // https://docs.railway.app/guides/fixing-common-errors 
    await fastify.listen({
      port: Number(PORT),
      host: HOST // <- important to make it work on Railway
    });
    console.log(`server started on port ${PORT}`)
  } catch (err) {
    fastify.log.error(err);
    console.error(err)
    process.exit(1);
  }
};

main();