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

export const TmdbConfiguration = z.object({
  images: ImagesConfigurationSchema.nullish(),
  change_keys: z.array(z.string()).nullish(),
});
export type TmdbConfiguration = z.infer<typeof TmdbConfiguration>;

const ApiResponse = z.discriminatedUnion("status", [
  z.object({ status: z.literal(200), body: TmdbConfiguration }),
]);
type ApiResponse = z.infer<typeof ApiResponse>;

export const configuration = makeFetcher({
  endpoint: () => "/configuration",
  queryParams: z.object({}).nullish(),
  response: ApiResponse,
});

export const toImageUrl = (
  config: TmdbConfiguration,
  posterPath: string | undefined | null,
  sizeIndex?: number
): string | null => {
  if (!posterPath) {
    return null;
  }

  const base = config?.images?.secure_base_url;

  if (!base) {
    return null;
  }

  const posterSizes = config?.images?.poster_sizes;

  if (!posterSizes) {
    return null;
  }

  const posterSize = posterSizes?.[sizeIndex ?? posterSizes?.length - 1 ?? 0];

  if (!posterSize) {
    return null;
  }

  return `${base}${posterSize}${posterPath}`;
};

export const toVideoSrc = (data: { site?: string; key?: string }) => {
  const { site, key } = data;
  if (!key) {
    return null;
  }

  if (!site) {
    return null;
  }

  if (site === "YouTube") {
    return `https://www.youtube.com/embed/${key}`;
  }

  return null;
};
