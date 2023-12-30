import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as FeedClient from "./@child/feed/feed.client";
import * as MediaClient from "./@child/media/media.client";
import { RouteProvider, useRoute } from "./@shared/routing";

const queryClient = new QueryClient();

export const App = () => {
  return (
    <RouteProvider>
      <QueryClientProvider client={queryClient}>
        <div className="w-screen h-[100dvh] flex-col bg-black flex items-center justify-center text-white overflow-hidden box-border">
          <div className="w-full h-full max-w-2xl max-h-[840px] border rounded overflow-hidden flex flex-col">
            <Page />
          </div>
        </div>
      </QueryClientProvider>
    </RouteProvider>
  );
}

const Page = () => {
  const route = useRoute()

  switch (route.value.t) {
    case "feed": {
      return <FeedClient.FeedPage />;
    }

    case "media-details": {
      return <MediaClient.MediaPage mediaId={route.value.mediaId} />;
    }
  }
}
