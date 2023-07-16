import { z } from "zod";
import { ErrResponse, makeFetcher } from "../shared";
import { TmdbCombinedCreditsOkResponse } from "./combined_credits";

// https://developers.themoviedb.org/3/people/get-person-details

export const TmdbPersonDetailsQueryParams = z.object({
  language: z
    .string()
    .min(2)
    .regex(/([a-z]{2})-([A-Z]{2})/)
    .default("en-US")
    .nullish(),
  append_to_response: z.string().nullish(),
});

const OkResponse = z.object({
  birthday: z.string().nullable().nullish(),
  known_for_department: z.string().nullish(),
  deathday: z.string().nullable().nullish(),
  id: z.number().int().nullish(),
  name: z.string().nullish(),
  also_known_as: z.array(z.string()).nullish(),
  gender: z.number().int().min(0).max(3).nullish(),
  biography: z.string().nullish(),
  popularity: z.number().nullish(),
  place_of_birth: z.string().nullable().nullish(),
  profile_path: z.string().nullable().nullish(),
  adult: z.boolean().nullish(),
  imdb_id: z.string().nullish(),
  homepage: z.string().nullable().nullish(),
  combined_credits: TmdbCombinedCreditsOkResponse.nullish(),
});

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: OkResponse }),
  z.object({ status: z.literal(401), body: ErrResponse }),
  z.object({ status: z.literal(404), body: ErrResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const personDetails = makeFetcher({
  endpoint: ({ id }: { id: number }) => `/person/${id}`,
  queryParams: TmdbPersonDetailsQueryParams,
  response: ApiResponse,
});
