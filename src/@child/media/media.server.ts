import { Err, Ok, Result, isErr } from "../../@shared/result";
import { Server } from "../../@shared/server";
import { tmdbClient } from "../../@shared/tmdb-client";
import { MediaPageData, contract } from "./media.contract";

export const mediaRouter = ({ s }: { s: Server }) => {
  return s.router(contract, {
    mediaPage: async (input) => {
      const got = await getMediaPageData(input.body)

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
        body: got.data,
      }
    },
  });
}



const getMediaPageData = async ({ mediaId }: { mediaId: string }): Promise<Result<MediaPageData, string>> => {
  const got = await tmdbClient.movie.details({
        pathParams: { id: parseInt(mediaId) },
        queryParams: {},
  })

  if (!got.success) {
    return Err("Something went wrong")
  }

  if (got.data.status !== 200) {
    return Err(got.data.body.status_message ?? "Something went wrong")
  }

  const movie = got.data.body

  const data: Partial<MediaPageData> = {
    backgroundUrl: '',
    id: movie.id?.toString(),
    mediaId: movie.id?.toString(),
    mediaType: 'movie',
    posterUrl: '',
    thirdPartyVideoUrls: [],
    title: movie.title,
  }

  const parsed = MediaPageData.safeParse(data)

  if (!parsed.success) {
    return Err("Something went wrong")
  }

  return Ok(parsed.data)
}
