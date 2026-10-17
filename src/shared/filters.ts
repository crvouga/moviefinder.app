import { fieldText, type Entity, type FeedParams } from "./contracts";
export function localMatch(
  e: Entity,
  p: FeedParams,
  status?: { seen: boolean; sentiment: string },
  inLists: Set<string> = new Set(),
  providerFiltered = false,
) {
  if (e.kind !== "movie" && e.kind !== "tv") return p.q !== "" && e.kind === "person";
  if (p.type !== "both" && e.kind !== p.type) return false;
  if ((p.seen === "seen" && !status?.seen) || (p.seen === "unseen" && status?.seen)) return false;
  if (p.sentiment !== "all" && (status?.sentiment ?? "unrated") !== p.sentiment) return false;
  if (
    p.exclude.includes(e.id) ||
    (p.list && !inLists.has(p.list)) ||
    (p.notInList && inLists.has(p.notInList))
  )
    return false;
  if (
    p.genres.some((g) => !e.genres?.includes(`tmdb:genre:${g}`)) ||
    p.excludeGenres.some((g) => e.genres?.includes(`tmdb:genre:${g}`))
  )
    return false;
  if (
    (p.ratingMin != null && (e.rating ?? 0) < p.ratingMin) ||
    (p.ratingMax != null && (e.rating ?? 0) > p.ratingMax)
  )
    return false;
  if (
    (p.popularityMin != null && (e.popularity ?? 0) < p.popularityMin) ||
    (p.popularityMax != null && (e.popularity ?? 0) > p.popularityMax)
  )
    return false;
  if (p.votesMin != null && Number(e.data.voteCount ?? 0) < p.votesMin) return false;
  if (
    (p.yearMin != null && (e.year ?? 0) < p.yearMin) ||
    (p.yearMax != null && (e.year ?? 9999) > p.yearMax)
  )
    return false;
  if (p.language && e.data.language !== p.language) return false;
  const date = fieldText(e.data.releaseDate ?? "");
  if ((p.dateMin || p.dateMax) && !date) return false;
  if ((p.dateMin && date < p.dateMin) || (p.dateMax && date > p.dateMax)) return false;
  const runtime = Array.isArray(e.data.runtime)
    ? Number(e.data.runtime[0])
    : Number(e.data.runtime);
  if (
    !providerFiltered &&
    ((p.runtimeMin != null && (!runtime || runtime < p.runtimeMin)) ||
      (p.runtimeMax != null && (!runtime || runtime > p.runtimeMax)))
  )
    return false;
  if (!providerFiltered) {
    const numbers = (value: unknown) =>
      Array.isArray(value) ? value.filter((v): v is number => typeof v === "number") : [];
    if (
      p.cast.some((id) => !numbers(e.data.castIds).includes(id)) ||
      p.crew.some((id) => !numbers(e.data.crewIds).includes(id))
    )
      return false;
    const countryValue = e.data.originCountries ?? (e.kind === "tv" ? e.data.countries : undefined);
    const countries = Array.isArray(countryValue) ? countryValue : [];
    if (
      p.country &&
      !countries.some((c) =>
        typeof c === "string"
          ? c === p.country
          : !!c && typeof c === "object" && (c as Record<string, unknown>).iso_3166_1 === p.country,
      )
    )
      return false;
    const keywords = Array.isArray(e.data.keywords) ? e.data.keywords : [];
    if (
      p.keywords.some(
        (id) =>
          !keywords.some(
            (k) => !!k && typeof k === "object" && (k as Record<string, unknown>).id === id,
          ),
      )
    )
      return false;
    if (p.providers.length) {
      const providers = e.data.watchProviders as
          | {
              results?: Record<
                string,
                {
                  flatrate?: { provider_id: number }[];
                  rent?: { provider_id: number }[];
                  buy?: { provider_id: number }[];
                }
              >;
            }
          | undefined,
        region = providers?.results?.[p.region],
        available = [...(region?.flatrate ?? []), ...(region?.rent ?? []), ...(region?.buy ?? [])];
      if (!p.providers.some((id) => available.some((a) => a.provider_id === id))) return false;
    }
  }
  return true;
}
