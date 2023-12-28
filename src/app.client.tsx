
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { FeedPage } from "./@child/feed/feed.client";
import { useCurrentPage } from "./@shared/page";

const queryClient = new QueryClient();

export const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <div className="w-screen h-screen flex-col bg-black flex items-center justify-center text-white overflow-hidden box-border">
        <div className="w-full h-full max-w-2xl max-h-[840px] border rounded overflow-hidden flex flex-col">
          <Page />
        </div>
      </div>
    </QueryClientProvider>
  );
}

const Page = () => {
  const currentPage = useCurrentPage();

  switch (currentPage.value.t) {
    case "feed": {
      return <FeedPage />;
    }

    case "movie-details": {
      return <div>hello</div>
    }
  }
}
