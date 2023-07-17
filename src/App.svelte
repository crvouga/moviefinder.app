<script lang="ts">
  import { onMount } from "svelte";
  import FeedPage from "./feed/FeedPage.svelte";
  import { tmdb, TmdbConfiguration } from "./tmdb-client";

  let tmdbConfig: TmdbConfiguration | null = null;

  const load = async () => {
    const got = await tmdb.configuration({
      queryParams: {},
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      tmdbConfig = got.data.body;
    }
  };

  onMount(() => {
    load()
  })
</script>



<main>
  <FeedPage />
</main>





<style>
  main {
    position: fixed;
    top: 0;
    left: 0;
    width: 100dvw;
    height: 100dvh;
  }
</style>
