import { z } from "zod";
import { ErrResponse, makeFetcher } from "../shared";

const TmdbMovieDetailsQueryParams = z.object({
  language: z
    .string()
    .min(2)
    .regex(/([a-z]{2})-([A-Z]{2})/, { message: "Invalid language format" })
    .default("en-US")
    .nullish(),
  append_to_response: z
    .string()
    .regex(/([\w]+)/, { message: "Invalid append_to_response format" })
    .nullish(),
});

export type TmdbMovieDetailsQueryParamsType = z.infer<
  typeof TmdbMovieDetailsQueryParams
>;

export const TmdbMovieDetailsOkResponse = z.object({
  adult: z.boolean().nullish(),
  backdrop_path: z.string().nullable().nullish(),
  belongs_to_collection: z.nullable(z.object({})).nullish(),
  budget: z.number().int().nullish(),
  genres: z
    .array(
      z.object({
        id: z.number().int().nullish(),
        name: z.string().nullish(),
      })
    )
    .nullish(),
  id: z.number().int().nullish(),
  homepage: z.string().nullable().nullish(),
  imdb_id: z.string().nullable().nullish(),
  original_language: z.string().nullish(),
  original_title: z.string().nullish(),
  overview: z.string().nullable().nullish(),
  popularity: z.number().nullish(),
  poster_path: z.string().nullable().nullish(),
  production_companies: z
    .array(
      z.object({
        name: z.string().nullish(),
        id: z.number().int().nullish(),
        logo_path: z.string().nullable().nullish(),
        origin_country: z.string().nullish(),
      })
    )
    .nullish(),
  production_countries: z
    .array(
      z.object({
        iso_3166_1: z.string().nullish(),
        name: z.string().nullish(),
      })
    )
    .nullish(),
  release_date: z.string().nullish(),
  revenue: z.number().int().nullish(),
  runtime: z.number().int().nullable().nullish(),
  spoken_languages: z
    .array(
      z.object({
        iso_639_1: z.string().nullish(),
        name: z.string().nullish(),
      })
    )
    .nullish(),
  status: z
    .enum([
      "Rumored",
      "Planned",
      "In Production",
      "Post Production",
      "Released",
      "Canceled",
    ])
    .nullish(),
  tagline: z.string().nullable().nullish(),
  title: z.string().nullish(),
  video: z.boolean().nullish(),
  vote_average: z.number().nullish(),
  vote_count: z.number().int().nullish(),
});

export type TmdbMovieDetailsOkResponse = z.infer<
  typeof TmdbMovieDetailsOkResponse
>;

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: TmdbMovieDetailsOkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const movieDetails = makeFetcher({
  endpoint: ({ id }: { id: number }) => `/movie/${id}`,
  queryParams: TmdbMovieDetailsQueryParams,
  response: ApiResponse,
});
