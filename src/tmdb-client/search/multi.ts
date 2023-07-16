import { z } from "zod";
import {
  ErrResponse,
  API_KEY_REGEX,
  ISO_3166_1_REGEX,
  ISO_639_1_REGEX,
  makeFetcher,
} from "../shared";

// https://developers.themoviedb.org/3/search/multi-search

export const TmdbSearchMultiQueryParams = z.object({
  api_key: z.string().regex(API_KEY_REGEX).default("<<api_key>>").nullish(),
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

const TvListResult = z.object({
  poster_path: z.string().nullable().nullish(),
  popularity: z.number().nullish(),
  id: z.number().int().nullish(),
  overview: z.string().nullish(),
  backdrop_path: z.string().nullable().nullish(),
  vote_average: z.number().nullish(),
  media_type: z.literal("tv").nullish(),
  first_air_date: z.string().nullish(),
  origin_country: z.array(z.string()).nullish(),
  genre_ids: z.array(z.number()).nullish(),
  original_language: z.string().nullish(),
  vote_count: z.number().int().nullish(),
  name: z.string().nullish(),
  original_name: z.string().nullish(),
});

const PersonListResult = z.object({
  profile_path: z.string().nullable().nullish(),
  adult: z.boolean().nullish(),
  id: z.number().int().nullish(),
  media_type: z.literal("person").nullish(),
  known_for: z.array(z.union([MovieListResult, TvListResult])).nullish(),
  name: z.string().nullish(),
  popularity: z.number().nullish(),
});

const PersonKnownForResult = z.union([MovieListResult, TvListResult]);

const PersonListResultKnownFor = z
  .object({
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
  })
  .or(TvListResult);

const OkResponse = z.object({
  page: z.number().int().nullish(),
  results: z
    .array(z.union([MovieListResult, TvListResult, PersonListResult]))
    .nullish(),
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

export const multi = makeFetcher({
  endpoint: () => "/search/multi",
  queryParams: TmdbSearchMultiQueryParams,
  response: ApiResponse,
});
