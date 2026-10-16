// Provider taxonomies verified through the official TMDB genre endpoints.
export const movieGenreIds = [
  28, 12, 16, 35, 80, 99, 18, 10751, 14, 36, 27, 10402, 9648, 10749, 878, 10770, 53, 10752, 37,
];
export const tvGenreIds = [
  10759, 16, 35, 80, 99, 18, 10751, 10762, 9648, 10763, 10764, 10765, 10766, 10767, 10768, 37,
];
export function supportsGenres(type: "movie" | "tv", genres: readonly number[]) {
  const valid = type === "movie" ? movieGenreIds : tvGenreIds;
  return genres.every((id) => valid.includes(id));
}
