import { useQuery } from "@tanstack/react-query";
import { FeedItem, contract } from "./feed.contract";
import { createClient } from "../../@shared/client";
import { useState } from "react";
import { Swiper, SwiperSlide } from 'swiper/react';

// Import Swiper styles
import 'swiper/css';


const client = createClient(contract) 

client.addComment({
  body: {
    mediaId: 'mediaId',
    text: 'text',
  }
})

export const FeedPage = ({openMediaDetails}: {openMediaDetails: () => void}) => {
  const [page] = useState(0)
  const [pageSize] = useState(10)
  const { data } = useQuery({
    queryKey: ["feed", page, pageSize],
    queryFn: () => client.feed({
      query: {
        page: String(page),
        pageSize: String(pageSize),
      }
    }),
  })

  if(!data) {
    return <div>loading...</div>
  }

  if(data?.status !== 200) {
    return <div>error</div>
  }

  return (
    <Swiper
    className="w-full h-full"
    spaceBetween={50}
    slidesPerView={1}
    direction="vertical"
    onSlideChange={() => console.log('slide change')}
    onSwiper={(swiper) => console.log(swiper)}
  >
    {data.body.items.map((feedItem) => (
      <SwiperSlide key={feedItem.id}>
        <ViewFeedItem feedItem={feedItem} />
      </SwiperSlide>
    ))}
    
  
  </Swiper>
  )

  
}

const ViewFeedItem = ({feedItem}: {feedItem: FeedItem}) => {
  return <div className="w-full h-full flex flex-col">
              <iframe
              className="w-full h-96"
              src={feedItem.thirdPartyVideoUrls[0]}
              frame-border="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              ></iframe>
              <button className="w-full p-4">
                
              </button>
  </div>
}