import { z } from "zod";
import { makeFetcher } from "../shared";

const ImagesConfigurationSchema = z.object({
  base_url: z.string().optional(),
  secure_base_url: z.string().optional(),
  backdrop_sizes: z.array(z.string()).optional(),
  logo_sizes: z.array(z.string()).optional(),
  poster_sizes: z.array(z.string()).optional(),
  profile_sizes: z.array(z.string()).optional(),
  still_sizes: z.array(z.string()).optional(),
  change_keys: z.array(z.string()).optional(),
});

const OkResponse = z.object({
  images: ImagesConfigurationSchema.optional(),
  change_keys: z.array(z.string()).optional(),
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
