<script lang="ts">
  import { onMount } from "svelte";
  import FeedItemVideoItem from "./FeedItemVideoItem.svelte";
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
  <swiper-container direction="horizontal" class="w-full h-full">
    {#each videos as video (video.key)}
      <swiper-slide class="w-full h-full">
          <FeedItemVideoItem {movie} {video} />
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
