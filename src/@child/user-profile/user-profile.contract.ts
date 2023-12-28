import { initContract } from '@ts-rest/core';
import { z } from 'zod';

const c = initContract();

export const contract = c.router({
  feed: {
    method: "GET",
    path: "/feed",
    query: z.object({
      page: z.string(),
      pageSize: z.string(),
    }),
    responses: {
      200: z.object({
        items: z.array(z.object({})),
      }),
      500: z.object({
        error: z.string(),
      }),
    },
  },
});

