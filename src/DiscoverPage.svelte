<script lang="ts">
  import { onMount } from "svelte";
  import {
    TmdbConfiguration,
    TmdbMovieListResult,
    tmdb,
    toImageUrl,
  } from "./tmdb-client";

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
      },
      pathParams: undefined,
    });

    if (got.success && got.data.status === 200) {
      movies = got.data.body.results;
    }
  });

  //
  //
  //
  //
</script>

<swiper-container direction="vertical">
  {#each movies as movie}
    <swiper-slide>
      <img
        class="w-full h-full object-cover user-select-none pointer-events-none"
        alt=""
        src={toImageUrl(tmdbConfig, movie.poster_path)}
      />
    </swiper-slide>
  {/each}
</swiper-container>

<style>
  swiper-container {
    width: 100%;
    height: 100%;
    cursor: move;
  }

  swiper-slide {
    text-align: center;
    font-size: 18px;
    background: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  swiper-slide img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
</style>
