import { createTmdb } from "../src/server/entity-store-sync/tmdb";
import { normalize, sourceDocument } from "../src/server/entity-store/normalize";
const token = process.env.TMDB_API_READ_ACCESS_TOKEN;
if (!token)
  throw new Error(
    "TMDB_API_READ_ACCESS_TOKEN is unavailable from Vault; live smoke check not performed",
  );
const request = createTmdb(token),
  cases = [
    {
      id: "tmdb:movie:550",
      path: "movie/550",
      append: "credits,recommendations,external_ids,videos,keywords,watch/providers",
    },
    {
      id: "tmdb:tv:1399",
      path: "tv/1399",
      append: "aggregate_credits,recommendations,external_ids,videos,keywords,watch/providers",
    },
    { id: "tmdb:tv:1399:season:1", path: "tv/1399/season/1", append: "credits" },
    {
      id: "tmdb:tv:1399:season:1:episode:1",
      path: "tv/1399/season/1/episode/1",
      append: "credits",
    },
    { id: "tmdb:person:819", path: "person/819", append: "combined_credits,external_ids" },
  ];
for (const value of cases) {
  const payload = sourceDocument.parse(
      await request(value.path, { append_to_response: value.append }),
    ),
    entities = normalize(value.id, payload, 1),
    main = entities.find((e) => e.id === value.id);
  if (!main?.title || main.title === "Details unavailable")
    throw new Error(`Missing primary projection for ${value.id}`);
  console.log(
    JSON.stringify({ id: value.id, normalizedEntities: entities.length, status: "passed" }),
  );
}
console.log(
  "Live TMDB movie, TV, season, episode, and person contracts passed; no database writes.",
);
