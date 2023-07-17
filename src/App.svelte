<script lang="ts">
  import FeedPage from "./feed/FeedPage.svelte";
  import { tmdb, TmdbConfiguration } from "./tmdb-client";

  let configuration: TmdbConfiguration | null = null;

  const load = async () => {
    const got = await tmdb.configuration({
      queryParams: {},
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      configuration = got.data.body;
    }

    console.log(got);
  };

  $: {
    load();
  }
</script>


<main class="max-w-xl w-screen h-screen max-h-full overflow-hidden mx-auto">
  {#if configuration}
    <FeedPage tmdbConfig={configuration} />
  {:else}
    <div>loading</div>
  {/if}
</main>


