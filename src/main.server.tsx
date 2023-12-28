import fastify from 'fastify';
import { initServer } from '@ts-rest/fastify';
import { appRouter } from './app.server';

const s = initServer()

const app = fastify();

const router =appRouter({s})

app.register(s.plugin(router.feed));

const start = async () => {
  try {
    await app.listen({ port: 3000 });
    console.log('server started')
  } catch (err) {
    app.log.error(err);
    process.exit(1);
  }
};

start();