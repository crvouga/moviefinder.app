import { useInfiniteQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Swiper as SwiperType } from 'swiper';
import 'swiper/css';
import 'swiper/css/virtual';
import { Controller, } from 'swiper/modules';
import { Swiper, SwiperSlide } from 'swiper/react';
import { z } from "zod";
import { createClient } from "../../@shared/client";
import { useRoute } from "../../@shared/routing";
import { Spinner } from "../../@shared/ui/spinner";
import { useQueryParamState } from "../../@shared/use-query-param";
import { FeedItem, contract } from "./feed.contract";

const client = createClient(contract)

const MAX_PAGE_SIZE = 20

export const FeedPage = () => {
  const [swiper_controller, set_swiper_controller] = useState<SwiperType | null>(null);


  const query_param_state = useQueryParamState({
    key: "feed_item_position",
    parser: z.object({
      active_page_index: z.number().int().nonnegative(),
      active_page_item_index: z.number().int().nonnegative(),
    }),
    initialValue: {
      active_page_index: 0,
      active_page_item_index: 0,
    },
  })

  const { data, fetchNextPage, fetchPreviousPage } = useInfiniteQuery({
    queryKey: ["feed"],
    queryFn: (input) => {
      return client.feed({
        body: {
          page_index: input.pageParam,
        }
      })
    },
    initialPageParam: query_param_state.value.active_page_index,
    getNextPageParam: (lastPage) => {
      if (lastPage.status === 200) {
        return lastPage.body.page_index + 1
      }
      return 0
    },
    getPreviousPageParam: (firstPage) => {
      if (firstPage.status === 200) {
        return Math.max(0, firstPage.body.page_index - 1)
      }
      return 0
    },
  })


  const swiper_items = (data?.pages ?? []).flatMap((page, relative_page_index) => {
    if (page.status !== 200) {
      return []
    }

    const page_index = page.body.page_index

    return page.body.items.map((item, page_item_index) => {
      const swiper_item_index = relative_page_index * MAX_PAGE_SIZE + page_item_index
      return {
        item,
        swiper_item_index,
        page_index,
        page_item_index,
      } as const
    })
  })


  const active_swiper_index = swiper_items.findIndex(swiper_item => swiper_item.page_item_index === query_param_state.value.active_page_item_index && swiper_item.page_index === query_param_state.value.active_page_index)

  useEffect(() => {
    if (active_swiper_index === -1) {
      return
    }

    const is_first_item_active = active_swiper_index === 0

    if (is_first_item_active) {
      fetchPreviousPage()
    }

    const is_last_item_active = active_swiper_index === swiper_items.length - 1

    if (is_last_item_active) {
      fetchNextPage()
    }

  }, [active_swiper_index])

  useEffect(() => {
    if (!swiper_controller) {
      return
    }
    if (active_swiper_index === -1) {
      return
    }
    swiper_controller.slideTo(active_swiper_index, 0)
  }, [active_swiper_index, swiper_controller])




  return (
    <Swiper
      className="w-full h-full"
      slidesPerView={1}
      direction="vertical"
      modules={[Controller]}
      controller={{ control: swiper_controller }}
      onSwiper={set_swiper_controller}
      onSlideChange={(swiper) => {
        const active_swiper_item = swiper_items[swiper.activeIndex]

        if (active_swiper_item === undefined) {
          return
        }

        query_param_state.push({
          active_page_index: active_swiper_item.page_index,
          active_page_item_index: active_swiper_item.page_item_index,
        })
      }}
    >
      {swiper_items.flatMap((item) => (
        <SwiperSlide key={item.item.id} >
          <ViewFeedItem
            isMounted={Math.abs(item.swiper_item_index - active_swiper_index) <= 1}
            feedItem={item.item}
          />
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

const ViewFeedItem = ({ feedItem, isMounted }: { feedItem: FeedItem, isMounted: boolean, }) => {
  if (isMounted) {
    return <ViewFeedItemActive feedItem={feedItem} />
  }

  return null
}

const ViewFeedItemActive = ({ feedItem }: { feedItem: FeedItem, }) => {
  const route = useRoute()
  return (
    <div className="w-full h-full flex flex-col">
      {true && <iframe
        className="w-full h-96 select-none bg-neutral-700"
        src={feedItem.thirdPartyVideoUrls[0]}
        frame-border="0"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
      ></iframe>}
      <button className="w-full p-4" onClick={() => route.push({ t: 'media-details', from: 'feed', mediaId: feedItem.mediaId })}>
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