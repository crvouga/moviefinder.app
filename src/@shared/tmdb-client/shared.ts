import { z, ZodSchema } from "zod";

//  https://www.themoviedb.org/settings/api
// example API request: https://api.themoviedb.org/3/movie/550?api_key=cf875963864a2cef08d67ed92cff7703
export const TMDB_API_KEY_V3 = "cf875963864a2cef08d67ed92cff7703";
export const TMDB_API_KEY_V4 =
  "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJjZjg3NTk2Mzg2NGEyY2VmMDhkNjdlZDkyY2ZmNzcwMyIsInN1YiI6IjVlNmFkNzg2MzU3YzAwMDAxMzNmZDEwMyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.Z4FnQZCCKKA5NaMg_H6ZFzytDSXLeEEUGotDQ7BqGsU";
export const BASE_URL = "https://api.themoviedb.org/3";

export const API_KEY_REGEX = /^[a-f0-9]{32}$/;
export const ISO_639_1_REGEX = /^[a-z]{2}$/;
export const ISO_3166_1_REGEX = /^[A-Z]{2}$/;

export function objectToStringMap<T extends { [s: string]: unknown }>(
  obj: T
): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, value] of Object.entries(obj)) {
    result[key] = String(value);
  }
  return result;
}

export const makeFetcher =
  <
    QueryParams extends Record<string, unknown>,
    ResponseBody extends Record<string, unknown>,
    PathParams = undefined
  >({
    endpoint,
    response,
  }: {
    endpoint: (pathParams: PathParams) => string;
    queryParams: ZodSchema<QueryParams>;
    response: ZodSchema<ResponseBody>;
  }) =>
  async (input: { pathParams: PathParams; queryParams: QueryParams }) => {
    const url = new URL(`${BASE_URL}${endpoint(input.pathParams)}`);
    const searchParams = new URLSearchParams(
      objectToStringMap(input.queryParams)
    );
    searchParams.set("api_key", TMDB_API_KEY_V3);
    url.search = searchParams.toString();
    console.log("[tmdb] GET", url.toString(), "");
    const fetched = await fetch(url.toString());
    const status = fetched.status;
    const body = await fetched.json();
    const parsed = await response.safeParseAsync({ status, body });
    return parsed;
  };

export const ErrResponse = z.object({
  status_message: z.string().optional(),
  status_code: z.number().int().optional(),
});

export type ErrResponse = z.infer<typeof ErrResponse>;
