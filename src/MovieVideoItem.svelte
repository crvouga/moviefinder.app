<script lang="ts">
  import {
    TmdbMovieVideo
  } from "./tmdb-client";


  export let video: TmdbMovieVideo;



  const uploadYouTubeVideo = async () => {
    console.log(video)

  }

</script>


<div class="w-full h-full flex items-center justify-center">
    {video.key}
    <button on:click={uploadYouTubeVideo}> Upload </button>
</div>


