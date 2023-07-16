<script lang="ts">
  import DiscoverPage from "./DiscoverPage.svelte";
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

<main
  class="bg-neutral-950 text-white grid place-items-center w-screen h-screen overflow-hidden"
>
  <div class="max-w-xl h-full max-h-full overflow-x-hidden">
    {#if configuration}
      <DiscoverPage tmdbConfig={configuration} />
    {:else}
      <div>loading</div>
    {/if}
  </div>
</main>
