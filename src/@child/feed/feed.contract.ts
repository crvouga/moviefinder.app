import { initContract } from '@ts-rest/core';
import { z } from 'zod';

const c = initContract();

const FeedItem = z.object({
  id: z.string(),
  title: z.string(),
})

export type FeedItem = z.infer<typeof FeedItem>;

export const contract = c.router({
  feed: {
    method: "GET",
    path: "/feed",
    responses: {
      200: z.object({
        items: z.array(FeedItem),
      }),
      500: z.object({
        error: z.string(),
      }),
    },
  },
});

