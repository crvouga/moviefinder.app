import { z } from "zod";
import {
  API_KEY_REGEX,
  ErrResponse,
  ISO_3166_1_REGEX,
  ISO_639_1_REGEX,
  makeFetcher,
} from "../shared";

// https://developers.themoviedb.org/3/search/multi-search

export const TmdbSearchMovieQueryParams = z.object({
  language: z.string().regex(ISO_639_1_REGEX).default("en-US").nullish(),
  query: z.string().min(0).nullish(),
  page: z.number().int().min(1).max(1000).default(1).nullish(),
  include_adult: z.boolean().nullish(),
  region: z.string().regex(ISO_3166_1_REGEX).nullish(),
});

const MovieListResult = z.object({
  poster_path: z.string().nullable().nullish(),
  adult: z.boolean().nullish(),
  overview: z.string().nullish(),
  release_date: z.string().nullish(),
  original_title: z.string().nullish(),
  genre_ids: z.array(z.number()).nullish(),
  id: z.number().int().nullish(),
  media_type: z.literal("movie").nullish(),
  original_language: z.string().nullish(),
  title: z.string().nullish(),
  backdrop_path: z.string().nullable().nullish(),
  popularity: z.number().nullish(),
  vote_count: z.number().int().nullish(),
  video: z.boolean().nullish(),
  vote_average: z.number().nullish(),
});

const OkResponse = z.object({
  page: z.number().int().nullish(),
  results: z.array(MovieListResult).nullish(),
  total_results: z.number().int().nullish(),
  total_pages: z.number().int().nullish(),
});

export type OkResponse = z.infer<typeof OkResponse>;

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: OkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const movie = makeFetcher({
  endpoint: () => "/search/movie",
  queryParams: TmdbSearchMovieQueryParams,
  response: ApiResponse,
});
