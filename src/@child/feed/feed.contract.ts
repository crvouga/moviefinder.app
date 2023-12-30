import { initContract } from '@ts-rest/core';
import { z } from 'zod';

const c = initContract();

export const FeedItem = z.object({
  id: z.string(),
  title: z.string(),
  posterUrl: z.string(),
  thirdPartyVideoUrls: z.array(z.string()),
  mediaId: z.string(),
})

export type FeedItem = z.infer<typeof FeedItem>;

export const contract = c.router({  
  feed: {
    method: "POST",
    path: "/feed",
    body: z.object({
      page_index: z.number(),      
    }),
    responses: {
      200: z.object({
        items: z.array(FeedItem),
        page_index: z.number(),
      }),
      500: z.object({
        error: z.string(),
      }),
    },
  },
  addComment: {
    method: "POST",
    path: "/add-comment",
    body: z.object({
      text: z.string(),
      mediaId: z.string(),
    }),
    responses: {
      200: z.object({
        commentId: z.string(),
      }),
      500: z.object({
        error: z.string(),
      }),
    },
  },
});