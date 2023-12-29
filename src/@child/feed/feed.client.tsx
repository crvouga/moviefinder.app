import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import 'swiper/css';
import 'swiper/css/virtual';
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
  const { data, fetchNextPage } = useInfiniteQuery({
    queryKey: ["feed"],
    queryFn: (input) => {
      console.log({ input })
      return client.feed({
        query: {
          page: input.pageParam.toString(),
          pageSize: String(10),
        }
      })
    },
    initialPageParam: 1,
    getNextPageParam: (lastPage) => {
      console.log({ lastPage })
      if (lastPage.status === 200) {
        return lastPage.body.page + 1
      }
      return 1
    },
    getPreviousPageParam: (firstPage) => {
      // console.log({ firstPage })
      if (firstPage.status === 200) {
        return firstPage.body.page - 1
      }
      return 1
    },
  })

  const feedItems = data?.pages.flatMap(page => page.status === 200 ? page.body.items : []) ?? []

  const [activeIndex, setActiveIndex] = useState(0)

  useEffect(() => {
    const isEnd = Math.abs(activeIndex - feedItems.length) < 1
    if (isEnd) {
      fetchNextPage()
    }
  }, [activeIndex])

  return (
    <Swiper
      className="w-full h-full"
      spaceBetween={50}
      slidesPerView={1}
      direction="vertical"
      onSlideChange={(swiper) => {
        setActiveIndex(swiper.activeIndex)
      }}
    // virtual
    >
      {feedItems.map((feedItem, index) => (
        <SwiperSlide key={feedItem.id}>
          <ViewFeedItem feedItem={feedItem} index={index} activeIndex={activeIndex} />
        </SwiperSlide>
      ))}
      <SwiperSlide>
        <div>
          Loading...
        </div>
      </SwiperSlide>
    </Swiper>
  )
}

const ViewFeedItem = ({ feedItem, index, activeIndex }: { feedItem: FeedItem, index: number, activeIndex: number }) => {
  if (Math.abs(index - activeIndex) > 1) {
    return null
  }

  return <div className="w-full h-full flex flex-col">
    {feedItem.title}
    <iframe
      className="w-full h-96 select-none"
      src={feedItem.thirdPartyVideoUrls[0]}
      frame-border="0"
      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
    ></iframe>
    <button className="w-full p-4">
    </button>
  </div>
}