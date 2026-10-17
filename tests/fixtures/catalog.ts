export const movie = {
  id: 550,
  title: "Fight Club",
  overview: "An insomniac office worker and a soap maker form an underground fight club.",
  release_date: "1999-10-15",
  poster_path: "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
  genre_ids: [18, 53],
  popularity: 95,
  vote_average: 8.4,
  vote_count: 29000,
  original_language: "en",
  runtime: 139,
  origin_country: ["US"],
};
export const television = {
  id: 1399,
  name: "Game of Thrones",
  overview: "Noble families vie for control of the Iron Throne.",
  first_air_date: "2011-04-17",
  poster_path: "/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
  genre_ids: [18, 10765],
  popularity: 80,
  vote_average: 8.5,
  vote_count: 24000,
  original_language: "en",
};
export const movieDetail = {
  ...movie,
  genres: [
    { id: 18, name: "Drama" },
    { id: 53, name: "Thriller" },
  ],
  credits: {
    cast: [
      {
        id: 819,
        name: "Edward Norton",
        character: "The Narrator",
        credit_id: "actor-one",
        profile_path: null,
      },
    ],
    crew: [
      {
        id: 7467,
        name: "David Fincher",
        job: "Director",
        department: "Directing",
        credit_id: "director-one",
      },
    ],
  },
  recommendations: {
    results: [{ id: 680, title: "Pulp Fiction", media_type: "movie", release_date: "1994-09-10" }],
  },
  videos: { results: [{ key: "test-trailer", name: "Trailer", site: "YouTube" }] },
};
export const fixtures = Array.from({ length: 12 }, (_, i) => ({
  ...movie,
  id: i + 1,
  title: `Test story ${i + 1}`,
  popularity: 100 - i,
}));
export async function fixtureUpstream(path: string, params: Record<string, string> = {}) {
  if (path.startsWith("discover/movie") || path.startsWith("trending/movie"))
    return {
      page: Number(params.page ?? 1),
      total_pages: 2,
      results: fixtures.map((m) => ({
        ...m,
        id: m.id + (Number(params.page ?? 1) - 1) * 12,
        title: `Test story ${m.id + (Number(params.page ?? 1) - 1) * 12}`,
        popularity: m.popularity - (Number(params.page ?? 1) - 1) * 12,
      })),
    };
  if (path.startsWith("discover/tv") || path.startsWith("trending/tv"))
    return { page: 1, total_pages: 1, results: [television] };
  if (path.startsWith("search/"))
    return {
      page: 1,
      total_pages: 1,
      results: [
        { ...movie, media_type: "movie" },
        { ...television, media_type: "tv" },
        { id: 819, name: "Edward Norton", media_type: "person" },
      ],
    };
  if (path.startsWith("person/"))
    return {
      id: Number(path.split("/")[1]),
      name: "Edward Norton",
      biography: "An actor.",
      combined_credits: { cast: [{ ...movie, media_type: "movie" }] },
    };
  if (path.includes("/episode/"))
    return {
      id: 1001,
      name: "Winter Is Coming",
      episode_number: 1,
      air_date: "2011-04-17",
      overview: "The first episode.",
      credits: { cast: [] },
    };
  if (path.includes("/season/"))
    return {
      id: 101,
      name: "Season 1",
      season_number: 1,
      episodes: [{ id: 1001, name: "Winter Is Coming", episode_number: 1, air_date: "2011-04-17" }],
    };
  if (path.startsWith("tv/"))
    return {
      ...television,
      seasons: [{ id: 101, name: "Season 1", season_number: 1 }],
      number_of_seasons: 8,
      number_of_episodes: 73,
    };
  const id = Number(path.split("/")[1]) || 550;
  if (id >= 1 && id <= 24)
    return { ...movieDetail, id, title: `Test story ${id}`, popularity: 101 - id };
  return { ...movieDetail, id, ...(id === 680 ? { title: "Pulp Fiction" } : {}) };
}
