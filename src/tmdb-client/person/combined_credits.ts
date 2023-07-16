import { z } from "zod";
import { ErrResponse, makeFetcher } from "../shared";

//

export const TmdbPersonCombinedCreditsQueryParams = z.object({
  language: z
    .string()
    .min(2, "language must have a minimum length of 2 characters")
    .regex(
      /^([a-z]{2})-([A-Z]{2})$/,
      'language must follow the format "xx-XX" (ISO 639-1)'
    )
    .default("en-US")
    .nullish(),
});

const CastAndCrew = z.object({
  id: z.number().nullish(),
  original_language: z.string().nullish(),
  episode_count: z.number().nullish(),
  overview: z.string().nullish(),
  origin_country: z.array(z.string()).nullish(),
  original_name: z.string().nullish(),
  genre_ids: z.array(z.number()).nullish(),
  name: z.string().nullish(),
  media_type: z.string().nullish(),
  poster_path: z.string().nullable().nullish(),
  first_air_date: z.string().nullish(),
  vote_average: z.number().nullish(),
  vote_count: z.number().nullish(),
  character: z.string().nullish(),
  backdrop_path: z.string().nullable().nullish(),
  popularity: z.number().nullish(),
  credit_id: z.string().nullish(),
  original_title: z.string().nullish(),
  video: z.boolean().nullish(),
  release_date: z.string().nullish(),
  title: z.string().nullish(),
  adult: z.boolean().nullish(),
});

export const TmdbCombinedCreditsOkResponse = z.object({
  cast: z.array(CastAndCrew).nullish(),
  crew: z.array(CastAndCrew).nullish(),
});

const TmdbCombinedCreditsResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: TmdbCombinedCreditsOkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);

export type TmdbCombinedCreditsResponse = z.infer<
  typeof TmdbCombinedCreditsResponse
>;

export const combined_credits = makeFetcher({
  endpoint: ({ id }: { id: number }) => `/person/${id}/combined_credits`,
  queryParams: TmdbPersonCombinedCreditsQueryParams,
  response: TmdbCombinedCreditsResponse,
});
