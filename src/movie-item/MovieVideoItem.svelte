<script lang="ts">
  import { getDownloadURL, ref } from "firebase/storage";
  import { storage } from "../firebase-client";
	import {
	  TmdbMovieListResult,
	  TmdbMovieVideo
	} from "../tmdb-client";
	import { uploadMovieVideoItem } from './movie-video-item';
  import { onMount } from "svelte";
  import {onViewportEnter}from "./on-viewport-enter"

  export let movie: TmdbMovieListResult;
  export let video: TmdbMovieVideo;


  let downloadUrl: string | null = null


const loadDownloadUrl = async () => {

  const videoRef = ref(storage, `movie-video-item/${video.key}.mp4`);
  downloadUrl = await getDownloadURL(videoRef)
}



  const uploadYouTubeVideo = async () => {

    if(movie.id && video.key)
{
await   uploadMovieVideoItem({
  tmdbMovieId: movie.id,
  youTubeVideoKey: video.key
})
  try {


  } catch(error) {
    console.error(String(error))
  }
}
  }



  function handleViewportEnter() {
    loadDownloadUrl()
  }

</script>


<div class="w-full h-full flex items-center justify-center"  use:onViewportEnter={{ once: true }} on:viewportenter={handleViewportEnter}>
    {#if downloadUrl}
      <!-- svelte-ignore a11y-media-has-caption -->
      <video  src={downloadUrl} controls></video>
    {:else}
      <button on:click={uploadYouTubeVideo}> Upload </button>
    {/if}
</div>


