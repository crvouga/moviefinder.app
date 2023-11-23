import { configuration } from "./configuration";
import { discover } from "./discover";
import { movie } from "./movie";
import { search } from "./search";
import { person } from "./person";

export const tmdbClient = {
  search,
  discover,
  configuration,
  movie,
  person,
};
