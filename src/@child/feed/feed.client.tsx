import { useEffect } from "react";
import { useCurrentPage } from "../../@shared/page";
import { tmdbClient } from "../../@shared/tmdb-client";

export function FeedPage() {
  const currentPage = useCurrentPage();



  async function get() {
    const got = await tmdbClient.discover.movie({
      pathParams:undefined,
      queryParams:{},
    })



  }

  useEffect(() => {
  get()
  }, [])




  return (
    <div className="w-full flex flex-col flex-1">
      <div className="flex-1">
        <h1 className="text-3xl font-bold underline">Hello world!</h1>;
        <button onClick={() => currentPage.push({ t: "movie-details" })}>
          got to movie details
        </button>
      </div>
      <div className="w-full flex items-center border-t">
        <button className="flex-1 p-5 border">down</button>

        <button className="flex-1 p-5 border">up</button>
      </div>
    </div>
  );
}
