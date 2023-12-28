import 'isomorphic-fetch'
import Fastify from 'fastify';
import { initServer } from '@ts-rest/fastify';
import { appRouter } from './app.server';
import cors from '@fastify/cors'


const s = initServer()

const fastify = Fastify();
fastify.register(cors, { 
  origin: true,
  methods: ['GET', 'PUT', 'POST', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true,
  preflightContinue: false,
  optionsSuccessStatus: 204
})

const router = appRouter({ s })

fastify.register(s.plugin(router.feed));

const start = async () => {
  try {
    await fastify.listen({ port: 3000 });
    console.log('server started')
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
};

start();