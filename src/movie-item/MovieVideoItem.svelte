<script lang="ts">
  import { getDownloadURL, ref } from "firebase/storage";
  import { storage } from "../firebase-client";
	import {
	  TmdbMovieListResult,
	  TmdbMovieVideo
	} from "../tmdb-client";
	import { uploadMovieVideoItem } from './movie-video-item';
  import { onMount } from "svelte";
  	import viewport from './on-viewport-enter';


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


<button class="w-full h-full overflow-hidden flex flex-col bg-black items-center justify-center" on:click={toggle}>
  <div class="w-full flex-1 overflow-hidden max-w-full flex items-center justify-center bg-black" >
    <!-- svelte-ignore a11y-media-has-caption -->
     <video
      src={downloadUrl}
      on:play={onPlay}
      on:pause={onPause}
      bind:this={videoEl}
      playsinline
      class="w-full h-full bg-black"
      />
  </div>
</button>

