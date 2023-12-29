import { z } from "zod";
import { useQueryParam } from "./@shared/use-query-param";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as FeedClient from "./@child/feed/feed.client";
import * as MediaClient from "./@child/media/media.client";

const queryClient = new QueryClient();

export const App = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <div className="w-screen h-[100dvh] flex-col bg-black flex items-center justify-center text-white overflow-hidden box-border">
        <div className="w-full h-full max-w-2xl max-h-[840px] border rounded overflow-hidden flex flex-col">
          <Page />
        </div>
      </div>
    </QueryClientProvider>
  );
}

const Page = () => {
  const page = useQueryParam({
    key: "appPage",
    parser: z.discriminatedUnion("t", [
      z.object({
        t: z.literal("feed"),
      }),
      z.object({
        t: z.literal("media-details"),
        from: z.enum(['feed']),
        mediaId: z.string(),
      }),
    ]),
    initialValue: {
      t: "feed",
    },
  });

  switch (page.value.t) {
    case "feed": {
      return <FeedClient.FeedPage onOpenMediaDetails={({ mediaId }) => page.push({ t: 'media-details', from: 'feed', mediaId: mediaId })} />;
    }

    case "media-details": {
      return <MediaClient.MediaPage mediaId={page.value.mediaId} />;
    }
  }
}
