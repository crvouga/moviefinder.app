import { z } from "zod";
import { ErrResponse, makeFetcher } from "../shared";

// https://developers.themoviedb.org/3/discover/movie-discover

export const TmdbDiscoverMovieQueryParams = z.object({
  //   api_key: z.string().nonempty(),
  language: z
    .string()
    .regex(/^[a-z]{2}-[A-Z]{2}$/)
    .default("en-US")
    .nullish(),
  region: z
    .string()
    .regex(/^[A-Z]{2}$/)
    .nullish(),
  sort_by: z
    .enum([
      "",
      "popularity.asc",
      "popularity.desc",
      "release_date.asc",
      "release_date.desc",
      "revenue.asc",
      "revenue.desc",
      "primary_release_date.asc",
      "primary_release_date.desc",
      "original_title.asc",
      "original_title.desc",
      "vote_average.asc",
      "vote_average.desc",
      "vote_count.asc",
      "vote_count.desc",
    ])
    .default("popularity.desc")
    .nullish(),
  certification_country: z.string().nullish(),
  certification: z.string().nullish(),
  "certification.lte": z.string().nullish(),
  "certification.gte": z.string().nullish(),
  include_adult: z.boolean().nullish(),
  include_video: z.boolean().nullish(),
  page: z.number().int().min(1).max(1000).default(1).nullish(),
  primary_release_year: z.number().int().nullish(),
  "primary_release_date.gte": z
    .string()
    .regex(/^(\d{4})-(\d{2})-(\d{2})$/)
    .nullish(),
  "primary_release_date.lte": z
    .string()
    .regex(/^(\d{4})-(\d{2})-(\d{2})$/)
    .nullish(),
  "release_date.gte": z
    .string()
    .regex(/^(\d{4})-(\d{2})-(\d{2})$/)
    .nullish(),
  "release_date.lte": z
    .string()
    .regex(/^(\d{4})-(\d{2})-(\d{2})$/)
    .nullish(),
  with_release_type: z.number().int().min(1).max(6).nullish(),
  year: z.number().int().nullish(),
  "vote_count.gte": z.number().int().min(0).nullish(),
  "vote_count.lte": z.number().int().min(1).nullish(),
  "vote_average.gte": z.number().min(0).nullish(),
  "vote_average.lte": z.number().min(0).nullish(),
  with_cast: z.string().nullish(),
  with_crew: z.string().nullish(),
  with_people: z.string().nullish(),
  with_companies: z.string().nullish(),
  with_genres: z.string().nullish(),
  without_genres: z.string().nullish(),
  with_keywords: z.string().nullish(),
  without_keywords: z.string().nullish(),
  "with_runtime.gte": z.number().int().nullish(),
  "with_runtime.lte": z.number().int().nullish(),
  with_original_language: z.string().nullish(),
  with_watch_providers: z.string().nullish(),
  watch_region: z.string().nullish(),
  with_watch_monetization_types: z
    .enum(["flatrate", "free", "ads", "rent", "buy"])
    .nullish(),
  without_companies: z.string().nullish(),
});

export type QueryParams = z.infer<typeof TmdbDiscoverMovieQueryParams>;

const MovieListResult = z.object({
  poster_path: z.string().nullable(),
  adult: z.boolean(),
  overview: z.string(),
  release_date: z.string().nullish(),
  genre_ids: z.array(z.number()),
  id: z.number(),
  original_title: z.string(),
  original_language: z.string(),
  title: z.string(),
  backdrop_path: z.string().nullable(),
  popularity: z.number(),
  vote_count: z.number(),
  video: z.boolean(),
  vote_average: z.number(),
});

const OkResponse = z.object({
  page: z.number().nullish(),
  results: z.array(MovieListResult),
  total_results: z.number().nullish(),
  total_pages: z.number().nullish(),
});

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: OkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const movie = makeFetcher({
  endpoint: () => "/discover/movie",
  queryParams: TmdbDiscoverMovieQueryParams,
  response: ApiResponse,
});
