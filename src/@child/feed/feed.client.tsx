import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import 'swiper/css';
import 'swiper/css/virtual';
import { Swiper, SwiperSlide } from 'swiper/react';
import { createClient } from "../../@shared/client";
import { FeedItem, contract } from "./feed.contract";
import { Spinner } from "../../@shared/ui/spinner";


const client = createClient(contract)

client.addComment({
  body: {
    mediaId: 'mediaId',
    text: 'text',
  }
})

export const FeedPage = ({ onOpenMediaDetails }: { onOpenMediaDetails: (input: { mediaId: string }) => void }) => {
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
    >
      {feedItems.map((feedItem, index) => (
        <SwiperSlide key={feedItem.id}>
          <ViewFeedItem feedItem={feedItem} index={index} activeIndex={activeIndex} onOpenMediaDetails={onOpenMediaDetails} />
        </SwiperSlide>
      ))}
      <SwiperSlide>
        <div className="w-full h-full flex items-center justify-center">
          <Spinner className="w-12 h-12" />
        </div>
      </SwiperSlide>
    </Swiper>
  )
}

const ViewFeedItem = ({ feedItem, index, activeIndex, onOpenMediaDetails, }: { feedItem: FeedItem, index: number, activeIndex: number, onOpenMediaDetails: (input: { mediaId: string }) => void, }) => {
  if (Math.abs(index - activeIndex) > 1) {
    return null
  }
  return <ViewFeedItemActive feedItem={feedItem} onOpenMediaDetails={onOpenMediaDetails} />
}

const ViewFeedItemActive = ({ feedItem, onOpenMediaDetails }: { feedItem: FeedItem, onOpenMediaDetails: (input: { mediaId: string }) => void }) => {
  return (
    <div className="w-full h-full flex flex-col">
      <iframe
        className="w-full h-96 select-none"
        src={feedItem.thirdPartyVideoUrls[0]}
        frame-border="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
      ></iframe>
      <button className="w-full p-4" onClick={() => onOpenMediaDetails({ mediaId: feedItem.mediaId })}>
        <div>
          <p className="text-3xl font-bold">
            {feedItem.title}
          </p>
          <p className="text-lg text-white/50">
          </p>
        </div>
      </button>
    </div>
  )
}