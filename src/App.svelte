<script lang="ts">
  import type { Movie } from "./movie";
  import { tmdb } from "./tmdb-client";

  let movies: Movie[] = [];

  const load = async () => {
    const got = await tmdb.discover.movie({
      queryParams: {
        page: 1,
      },
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      movies = got.data.body.results.map((x): Movie => {
        return {
          id: x.id,
          title: x.title,
        };
      });
    }

    console.log(got);
  };

  $: {
    load();
  }
</script>

<main>
  {#each movies as movie}
    <div>
      <h1>{movie.title}</h1>
    </div>
  {/each}
</main>

<style>
  .logo {
    height: 6em;
    padding: 1.5em;
    will-change: filter;
    transition: filter 300ms;
  }
  .logo:hover {
    filter: drop-shadow(0 0 2em #646cffaa);
  }
  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00aa);
  }
  .read-the-docs {
    color: #888;
  }
</style>
