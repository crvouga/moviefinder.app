// import { useEffect } from "react";
import { useEffect } from "react";
import { useCurrentPage } from "../../@shared/page";
// import { tmdbClient } from "../../@shared/tmdb-client";
import type { FeedRouter } from "./feed.server";


const client = new FeedRouter();


export function FeedPage() {
  const currentPage = useCurrentPage();

  const query = useQuery({
    queryKey: ["feed"],
    queryFn: async () => {
      const feed = await currentPage.query((query) =>
        query.feed.getFeed({
          page: 1,
        })
      );
      return feed;
    },
  })



  return (
    <div className="w-full flex flex-col flex-1">
      <div className="flex-1">
        <h1 className="text-3xl font-bold underline">Hello world!</h1>;
        <button onClick={() => currentPage.push({ t: "movie-details" })}>
          got to movie details
        </button>
      </div>
      {query.isLoading && <div>loading...</div>}
      {query.isError && <div>error</div>}
      {query.data && <div>{JSON.stringify(query.data)}</div>}
      <div className="w-full flex items-center border-t">
        <button className="flex-1 p-5 border">down</button>

        <button className="flex-1 p-5 border">up</button>
      </div>
    </div>
  );
}
