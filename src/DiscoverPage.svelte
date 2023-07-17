<script lang="ts">
  import { onMount } from "svelte";
  import MovieItem from "./movie-item/MovieItem.svelte";
  import { TmdbConfiguration, TmdbMovieListResult, tmdb } from "./tmdb-client";

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

<swiper-container direction="vertical" history>
  {#each movies as movie}
    <swiper-slide>
      <MovieItem {movie} {tmdbConfig} />
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
</style>


