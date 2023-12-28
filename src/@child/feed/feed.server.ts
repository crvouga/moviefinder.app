import { Server } from "../../@shared/server";
import { tmdbClient } from "../../@shared/tmdb-client";
import { FeedItem, contract } from "./feed.contract";

export const feedRouter = ({ s }: { s: Server }) => {
  return s.router(contract, {
    feed: async () => {
      const got = await tmdbClient.discover.movie({
        pathParams: undefined,
        queryParams: {
          page: 1,
          include_adult: false,
        }
      })

      if(!got.success) {
        return {
          status: 500,
          body: {
            error: "Something went wrong",
          }
        }
      }

      if(got.data.status !== 200) {
        return {
          status: 500,
          body: {
            error: got.data.body.status_message ?? "Something went wrong",
          }
        }
      }

      const feedItems = got.data.body.results.map((result): FeedItem => {
        return {
          id: result.id.toString(),
          title: result.title,
        }
      })


      return {
        status: 200,
        body: {
          items: feedItems
        }
      }
    },    
  });
}

