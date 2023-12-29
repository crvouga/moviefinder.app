import { useInfiniteQuery } from "@tanstack/react-query";
import 'swiper/css';
import 'swiper/css/virtual';
import { Virtual } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';
import { createClient } from "../../@shared/client";
import { FeedItem, contract } from "./feed.contract";


const client = createClient(contract)

client.addComment({
  body: {
    mediaId: 'mediaId',
    text: 'text',
  }
})

export const FeedPage = (_props: { openMediaDetails: () => void }) => {
  const { data } = useInfiniteQuery({
    queryKey: ["feed"],
    queryFn: ({ pageParam }) => client.feed({
      query: {
        page: String(pageParam),
        pageSize: String(10),
      }
    }),
    initialPageParam: 1,
    getNextPageParam: (lastPage) => lastPage.status === 200 ? lastPage.body.page + 1 : undefined,
    getPreviousPageParam: (firstPage) => firstPage.status === 200 ? Math.max(1, firstPage.body.page - 1) : undefined,
  })

  if (!data) {
    return <div>Loading...</div>
  }

  const feedItems = data.pages.flatMap(page => page.status === 200 ? page.body.items : [])

  return (
    <Swiper
      className="w-full h-full"
      spaceBetween={50}
      slidesPerView={1}
      direction="vertical"
      modules={[Virtual]}
    // virtual
    >
      {feedItems.map((feedItem, index) => (
        <SwiperSlide key={feedItem.id} virtualIndex={index}>
          <ViewFeedItem feedItem={feedItem} />
        </SwiperSlide>
      ))}
    </Swiper>
  )
}

const ViewFeedItem = ({ feedItem }: { feedItem: FeedItem }) => {
  return <div className="w-full h-full flex flex-col">
    <iframe
      // key={feedItem.id}
      className="w-full h-96 select-none"
      src={feedItem.thirdPartyVideoUrls[0]}
      frame-border="0"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
    ></iframe>

    <button className="w-full p-4">

    </button>
  </div>
}