<script lang="ts">
  import { getDownloadURL, ref } from "firebase/storage";
  import { storage } from "../firebase-client";
	import {
	  TmdbMovieListResult,
	  TmdbMovieVideo
	} from "../tmdb-client";
	import { uploadMovieVideoItem } from './feed-item';
  import { onMount } from "svelte";

  //
  //
  //
  //
  //
  //

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

let progress = 0

onMount(() => {
  if(videoEl)
  {videoEl.addEventListener("timeupdate", () => {
      if (!videoEl) {
        return;
      }
      progress = ((videoEl.currentTime / videoEl.duration) * 100);
    });}
})
  const onProgressInput = (progressNew: number) => {
    progress = progressNew
    videoEl.currentTime = (progress / 100) * videoEl.duration;
  };

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
<div class="w-full h-full flex flex-col">
  <button class="w-full flex-1 bg-black relative" on:click={toggle}>
    <!-- svelte-ignore a11y-media-has-caption -->
    <video
      src={downloadUrl}
      on:play={onPlay}
      on:pause={onPause}
      bind:this={videoEl}
      playsinline
      class="w-full h-full bg-black"
    />

    <div class="absolute top-0 left-0 w-full h-full grid place-items-center">
      {#if !playing}
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" class="w-14 h-14">
          <path fill-rule="evenodd" d="M4.5 5.653c0-1.426 1.529-2.33 2.779-1.643l11.54 6.348c1.295.712 1.295 2.573 0 3.285L7.28 19.991c-1.25.687-2.779-.217-2.779-1.643V5.653z" clip-rule="evenodd" />
        </svg>
      {/if}
    </div>
  </button>

  <input
      type="range"
      value={progress}
      on:input={(e) => onProgressInput(parseInt(e.currentTarget.value))}
      min="0"
      max="100"
      class="range range-primary"
    />
</div>
{:else}
<div class="w-full h-full flex flex-col bg-black items-center justify-center">
  <button on:click={uploadYouTubeVideo}>
    Download video
  </button>
  </div>
{/if}

