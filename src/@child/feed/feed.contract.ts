import { initContract } from '@ts-rest/core';
import { z } from 'zod';

const c = initContract();

const PostSchema = z.object({
  id: z.string(),
  title: z.string(),
  body: z.string(),
});

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
  createPost: {
    method: 'POST',
    path: '/posts',
    responses: {
      201: PostSchema,
    },
    body: z.object({
      title: z.string(),
      body: z.string(),
    }),
    summary: 'Create a post',
  },
  getPost: {
    method: 'GET',
    path: `/posts/:id`,
    responses: {
      200: PostSchema.nullable(),
    },
    summary: 'Get a post by id',
  },
});

