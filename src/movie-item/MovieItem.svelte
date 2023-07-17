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

<div class="w-full h-full relative overflow-hidden">
  <img
      class="w-full h-full object-cover user-select-none pointer-events-none"
      alt=""
      src={toImageUrl(tmdbConfig, movie.poster_path)}
  />


  <div class="absolute top-0 left-0 w-full h-full flex flex-col justify-end">
    <swiper-container direction="horizontal">
      {#each videos as video}
        <swiper-slide>
            <MovieVideoItem {movie} {video} />
        </swiper-slide>
      {/each}
    </swiper-container>
    <div class="bg-gradient-to-t from-black flex flex-col justify-end">
      <div class="w-full flex items-center overflow-hidden p-4 gap-4">
          <p class="text-xl font-bold text-left">
            {movie.title}
          </p>
      </div>
    </div>
  </div>
</div>


<style>
  swiper-container {
    width: 100%;
    height: 100%;
    cursor: move;
  }

  swiper-slide {
    width: 100%;
    height: 100%;
  }
</style>
