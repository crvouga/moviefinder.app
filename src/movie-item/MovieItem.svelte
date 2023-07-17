<script lang="ts">
  import { onMount } from "svelte";
  import MovieVideoItem from "./MovieVideoItem.svelte";
  import {
    TmdbConfiguration,
    TmdbMovieListResult,
    TmdbMovieVideo,
    tmdb,
    toImageUrl
  } from "../tmdb-client";


  //
  //
  //
  //

  export let tmdbConfig: TmdbConfiguration;
  export let movie: TmdbMovieListResult;

  //
  //
  //
  let videos: TmdbMovieVideo[] = []

  onMount(async () => {
    const got = await tmdb.movieVideo({
      pathParams: { movie_id: movie.id },
      queryParams: {},
    })
    if(got.success && got.data.status === 200) {
      videos = got.data.body.results

    }
  });

</script>

<div class="w-full h-full overflow-hidden flex flex-col">

  <div class="w-full h-full">
  <swiper-container direction="horizontal">
    {#each videos as video (video.key)}
      <swiper-slide>
          <MovieVideoItem {movie} {video} />
      </swiper-slide>
    {/each}
  </swiper-container>
  </div>


  <div class="bg-black flex flex-col justify-end w-full max-wfull">
    <div class="w-full flex items-center overflow-hidden p-4 gap-4">
        <p class="text-xl font-bold text-left">
          {movie.title}
        </p>
    </div>
  </div>

</div>


<style>
  swiper-container {
    width: 100% !important;
    height: 100% !important;
    max-width: 100% !important;
    min-width: 100% !important;
    max-height: 100% !important;
    min-height: 100% !important;
  }

  swiper-slide {
    width: 100% !important;
    height: 100% !important;
    max-width: 100% !important;
    min-width: 100% !important;
    max-height: 100% !important;
    min-height: 100% !important;
  }
</style>
