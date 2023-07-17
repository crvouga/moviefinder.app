<script lang="ts">
	import {
	  TmdbMovieListResult,
	  TmdbMovieVideo
	} from "../tmdb-client";
	import { uploadMovieVideoItem } from './movie-video-item';


  export let movie: TmdbMovieListResult;
  export let video: TmdbMovieVideo;



  const uploadYouTubeVideo = async () => {
    console.log(video)
    if(movie.id && video.key)
{uploadMovieVideoItem({
  tmdbMovieId: movie.id,
  youTubeVideoKey: video.key
})}
  }

</script>


<div class="w-full h-full flex items-center justify-center">
    {video.key}
    <button on:click={uploadYouTubeVideo}> Upload </button>
</div>


