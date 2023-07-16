import { z } from "zod";
import { makeFetcher } from "../shared";

const ImagesConfigurationSchema = z.object({
  base_url: z.string().nullish(),
  secure_base_url: z.string().nullish(),
  backdrop_sizes: z.array(z.string()).nullish(),
  logo_sizes: z.array(z.string()).nullish(),
  poster_sizes: z.array(z.string()).nullish(),
  profile_sizes: z.array(z.string()).nullish(),
  still_sizes: z.array(z.string()).nullish(),
  change_keys: z.array(z.string()).nullish(),
});

const OkResponse = z.object({
  images: ImagesConfigurationSchema.nullish(),
  change_keys: z.array(z.string()).nullish(),
});

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: OkResponse }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const configuration = makeFetcher({
  endpoint: () => "/configuration",
  queryParams: z.object({}),
  response: ApiResponse,
});
