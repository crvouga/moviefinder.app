<script lang="ts">
  import { onMount } from "svelte";
  import FeedItem from "./FeedItem.svelte";
  import { TmdbConfiguration, TmdbMovieListResult, tmdb } from "../tmdb-client";

  //
  //
  //
  //

  export let tmdbConfig: TmdbConfiguration;

  //
  //
  //
  //
  let movies: TmdbMovieListResult[] = [];
  onMount(async () => {
    const got = await tmdb.discoverMovie({
      queryParams: {
        page: 1,

        include_video: true,
      },
      pathParams: undefined,
    });


    if (got.success && got.data.status === 200) {
      movies = got.data.body.results;
      console.log(got.data.body)
    }
  });

  //
  //
  //
  //
</script>

<div class="w-full h-full">
  <swiper-container direction="vertical" class="w-full h-full">
    {#each movies.slice(0, 3) as movie (movie.id)}
      <swiper-slide class="w-full h-full">
        <FeedItem {movie} {tmdbConfig} />
      </swiper-slide>
    {/each}
  </swiper-container>
</div>
