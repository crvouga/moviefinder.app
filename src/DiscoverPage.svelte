<script lang="ts">
  import {
    TmdbConfiguration,
    TmdbMovieListResult,
    tmdb,
    toImageUrl,
  } from "./tmdb-client";
  import { onMount } from "svelte";

  export let tmdbConfig: TmdbConfiguration;

  let movies: TmdbMovieListResult[] = [];

  onMount(async () => {
    const got = await tmdb.discoverMovie({
      queryParams: {
        page: 1,
      },
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      movies = got.data.body.results;
    }
  });
</script>

<div
  class="w-full h-full overflow-x-hidden overflow-y-scroll snap-y snap-mandatory"
>
  {#each movies as movie}
    <div class="w-full h-full overflow-hidden cursor-move">
      <img
        class="w-full h-full object-cover user-select-none pointer-events-none"
        alt=""
        src={toImageUrl(tmdbConfig, movie.poster_path)}
      />
      <h1>{movie.title}</h1>
    </div>
  {/each}
</div>
