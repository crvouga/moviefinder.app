<script lang="ts">
  import {
    TmdbConfiguration,
    TmdbMovieListResult,
    tmdb,
    toImageUrl,
  } from "./tmdb-client";

  export let tmdbConfig: TmdbConfiguration;

  let movies: TmdbMovieListResult[] = [];

  const load = async () => {
    const got = await tmdb.discoverMovie({
      queryParams: {
        page: 1,
      },
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      movies = got.data.body.results;
    }

    console.log(got);
  };

  $: {
    load();
  }
</script>

{#each movies as movie}
  <div class="w-full h-full overflow-hidden">
    <img
      class="w-full h-full object-cover"
      alt=""
      src={toImageUrl(tmdbConfig, movie.poster_path)}
    />
    <h1>{movie.title}</h1>
  </div>
{/each}
