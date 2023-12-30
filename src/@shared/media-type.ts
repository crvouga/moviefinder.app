import { z } from "zod";


export const MediaType = z.enum(["movie", "tv"])

export type MediaType = z.infer<typeof MediaType>;