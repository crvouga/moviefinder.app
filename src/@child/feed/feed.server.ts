import { Err, Ok, Result, isErr } from "../../@shared/result";
import { Server } from "../../@shared/server";
import { tmdbClient, toTmdbImageUrl, toVideoSrc } from "../../@shared/tmdb-client";
import { TmdbMovieVideo } from "../../@shared/tmdb-client/movie/video";
import { FeedItem, contract } from "./feed.contract";

export const feedRouter = ({ s }: { s: Server }) => {
  return s.router(contract, {
    feed: async ({ body }) => {
      const got = await getFeedItems(body)
      
      if (isErr(got)) {
        return {
          status: 500,
          body: {
            error: got.error,
          }
        }
      }

      return {
        status: 200,
        body: {
          items: got.data,
          page_index: body.page_index,
        }
      }
    },

    addComment: async () => {
      return {
        status: 200,
        body: {
          commentId: "abc123",
        }
      }
    }
  });
}

const getFeedItems = async ({ page_index }: { page_index: number }): Promise<Result<FeedItem[], string>> => {
  const got = await tmdbClient.discover.movie({
    pathParams: undefined,
    queryParams: {
      page: page_index + 1,
      include_adult: false,
      include_video: true,
    }
  })

  
  if (!got.success) {
    return Err("Something went wrong")
  }
  
  if (got.data.status !== 200) {
    return Err(got.data.body.status_message ?? "Something went wrong")
  }
  
  const config = await tmdbClient.configuration({ pathParams: undefined, queryParams: {}, })

  if (!config.success || config.data.status !== 200 || !config.data.body.images?.base_url) {
    return Err("Something went wrong while loading image configuration.")
    
  }

  const movieVideoResponses = await Promise.all(
    got.data.body.results.map((movie) => {
      return tmdbClient.movie.video({
        pathParams: { movie_id: movie.id },
        queryParams: {},
      });
    })
  );

  const videosByMovieId = new Map<number, TmdbMovieVideo[]>();
  for (const response of movieVideoResponses) {
    if (!response.success) {
      continue;
    }

    if (response.data.status !== 200) {
      continue;
    }

    const results = response.data?.body?.results;

    if (!results) {
      continue;
    }

    const movieId = response.data.body.id;

    if (!movieId) {
      continue;
    }

    videosByMovieId.set(movieId, results);
  }

  const moviesWithVideos = got.data.body.results.map((movie) => {
    const videos = videosByMovieId.get(movie.id) ?? [];
    return { ...movie, videos };
  });

  const tmdbConfig = config.data.body

  const feedItems: FeedItem[] = moviesWithVideos.flatMap((result): FeedItem[] => {
    const posterPath = result.poster_path

    const posterUrl = toTmdbImageUrl(
      tmdbConfig,
      posterPath
    )

    if (!posterUrl) {
      return []
    }
    
    const thirdPartyVideoUrls = result.videos.flatMap((video) => {
      const src = toVideoSrc(video);
      if (!src) {
        return [];
      }
      return [src];
    })


    return [{
      id: result.id.toString(),
      title: result.title,
      posterUrl: posterUrl,
      thirdPartyVideoUrls: thirdPartyVideoUrls,
      mediaId: result.id.toString(),
    }]
  })


  return Ok(feedItems)
}