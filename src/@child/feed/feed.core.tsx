import { z } from "zod";

const Movie = z.object({
  id: z.string(),
  title: z.string(),
  description: z.string(),
  poster: z.string(),
  rating: z.number(),
  duration: z.number(),
  genres: z.array(z.string()),
  releaseDate: z.string(),
  actors: z.array(z.string()),
  director: z.string(),
  trailer: z.string(),
  images: z.array(z.string()),
})

const FeedItem = Movie

export const Feed = z.object({
  items: z.array(FeedItem)
})

