<script lang="ts">
  import { getDownloadURL, ref } from "firebase/storage";
  import { storage } from "../firebase-client";
	import {
	  TmdbMovieListResult,
	  TmdbMovieVideo
	} from "../tmdb-client";
	import { uploadMovieVideoItem } from './feed-item';
  import { onMount } from "svelte";



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

  }

  onMount(() => {
loadDownloadUrl()
  })

let videoEl:HTMLVideoElement
let playing = false
  const toggle = () => {
  if(videoEl.paused && !playing){

      videoEl.play()
    return
  }

  if(playing && !videoEl.paused){
    videoEl.pause()
    return
  }

  }

  const onPlay = () => {
    playing = true
  }

  const onPause = () => {
    playing = false
  }

</script>


{#if downloadUrl}
<button class="w-full h-full overflow-hidden flex flex-col bg-black items-center justify-center" on:click={toggle}>
  <!-- svelte-ignore a11y-media-has-caption -->
  <video
   src={downloadUrl}
   on:play={onPlay}
   on:pause={onPause}
   bind:this={videoEl}
   playsinline
   class="w-full h-full bg-black"
   />
  </button>
{:else}
<div class="w-full h-full overflow-hidden flex flex-col bg-black items-center justify-center">
  <button on:click={uploadYouTubeVideo}>
    Download video
  </button>
  </div>
{/if}

