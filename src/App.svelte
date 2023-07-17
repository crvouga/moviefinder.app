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

    console.log(got);
  };

  onMount(() => {
    load()
  })
</script>


{#if tmdbConfig}
  <main class="w-screen h-screen overflow-hidden">
    <FeedPage {tmdbConfig} />
  </main>
{/if}


