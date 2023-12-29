import { configuration } from "./configuration";
import { discover } from "./discover";
import { movie } from "./movie";
import { search } from "./search";
import { person } from "./person";
import { TmdbMovieVideo } from "./movie/video";

export const tmdbClient = {
  search,
  discover,
  configuration,
  movie,
  person,
};


type ExtractDataType<T> = T extends { success: true; data: infer U }
  ? U
  : never;

type Configuration = ExtractDataType<
  Awaited<ReturnType<typeof tmdbClient.configuration>>
>['body']

export const toTmdbImageUrl = (
  config: Configuration,
  posterPath: string | undefined | null
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
  const posterSize = posterSizes?.[posterSizes?.length - 1 ?? 0];
  if (!posterSize) {
    return null;
  }
  return `${base}${posterSize}${posterPath}`;
};


export function toVideoSrc(data: TmdbMovieVideo) {
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
}