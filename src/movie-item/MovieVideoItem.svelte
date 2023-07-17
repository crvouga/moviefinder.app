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

  try {

    const videoRef = ref(storage, `movie-video-item/${video.key}.mp4`);
    downloadUrl = await getDownloadURL(videoRef)
  } catch(error){
    console.log("error while loading download url")
  }
}



  const uploadYouTubeVideo = async () => {

    if(!movie.id || !video.key)
{
  return
}
await uploadMovieVideoItem({
  tmdbMovieId: movie.id,
  youTubeVideoKey: video.key
})
  loadDownloadUrl()
  }



  const handleViewportEnter = () => {
    loadDownloadUrl()
  }

let videoEl:HTMLVideoElement
  const onClick = () => {
if(videoEl.paused){

    videoEl.play()
  return
}
videoEl.pause()
  }

</script>


<div class="w-full h-full overflow-hidden flex items-center justify-center bg-black"  use:onViewportEnter={{ once: true }} on:viewportenter={handleViewportEnter}>
    {#if downloadUrl}
      <!-- svelte-ignore a11y-media-has-caption -->
      <video bind:this={videoEl} on:click={onClick} class="w-full h-full bg-black"  src={downloadUrl}></video>
    {:else}
      <button on:click={uploadYouTubeVideo}> Upload </button>
    {/if}
</div>


