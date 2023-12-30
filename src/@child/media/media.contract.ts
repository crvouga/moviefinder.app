import { initContract } from '@ts-rest/core';
import { z } from 'zod';
import { MediaType } from '../../@shared/media-type';

const c = initContract();

export const MediaPageData = z.object({
  id: z.string(),
  title: z.string(),
  posterUrl: z.string(),
  backgroundUrl: z.string(),
  thirdPartyVideoUrls: z.array(z.string()),
  mediaId: z.string(),
  mediaType: z.string(),
})

export type MediaPageData = z.infer<typeof MediaPageData>;

export const contract = c.router({  
  mediaPage: {
    method: "POST",
    path: "/media-page",
    body: z.object({
      mediaId: z.string(),      
      mediaType: MediaType
    }),
    responses: {
      200: MediaPageData,
      500: z.object({
        error: z.string(),
      }),
    },
  },
});