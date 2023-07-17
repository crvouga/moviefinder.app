<script lang="ts">
  import { onMount } from "svelte";
  import FeedItemVideoItem from "./FeedItemVideoItem.svelte";
  import {
    TmdbConfiguration,
    TmdbMovieListResult,
    TmdbMovieVideo,
    tmdb,
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

<div class="w-full h-full flex flex-col">
  <swiper-container direction="horizontal" class="w-full flex-1">
    {#each videos.slice(0, 3) as video (video.key)}
      <swiper-slide class="w-full h-full">
        <FeedItemVideoItem {movie} {video} />
      </swiper-slide>
    {/each}
  </swiper-container>

  <div class="bg-black flex flex-col justify-end w-full p-4 gap-4">
    <p class="text-xl font-bold text-left">
      {movie.title}
    </p>
  </div>
</div>
