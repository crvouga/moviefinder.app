import { z } from "zod";
import { ErrResponse, makeFetcher } from "../shared";

export const TmdbMovieVideoQueryParams = z.object({
  //   api_key: z.string()
  language: z
    .string()
    .min(2)
    .regex(
      /([a-z]{2})-([A-Z]{2})/,
      "Must follow the pattern ([a-z]{2})-([A-Z]{2})"
    )
    .default("en-US"),
});

export type TmdbMovieVideoQueryParams = z.infer<
  typeof TmdbMovieVideoQueryParams
>;

export const TmdbMovieVideo = z.object({
  id: z.string().nullish(),
  iso_639_1: z.string().nullish(),
  iso_3166_1: z.string().nullish(),
  name: z.string().nullish(),
  key: z.string().nullish(),
  site: z.string().nullish(),
  size: z.number().nullish(),
  type: z.string().nullish(),
  official: z.boolean().nullish(),
  published_at: z.string().nullish(),
});

export type TmdbMovieVideo = z.infer<typeof TmdbMovieVideo>;

const OkResponse = z.object({
  id: z.number().nullish(),
  results: z.array(TmdbMovieVideo).nullish(),
});

export type OkResponseType = z.infer<typeof OkResponse>;

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: OkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const movieVideo = makeFetcher({
  endpoint: ({ movie_id }: { movie_id: number }) => `/movie/${movie_id}/videos`,
  queryParams: TmdbMovieVideoQueryParams,
  response: ApiResponse,
});
