import { useQuery } from "@tanstack/react-query";
import { contract } from "./feed.contract";
import { createClient } from "../../@shared/client";
import { useState } from "react";

const client = createClient(contract) 

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

  
  return <div>
    {data.body.items.map(item => {
      return (
        <div key={item.id} onClick={() => openMediaDetails()}>
          <div>{item.title}</div>
          <FeedItem />
        </div>
      )
    })}
  </div>
}

const FeedItem = () => {
  return <div>
    <div>title</div>
    <div>description</div>
    <div>image</div>
    <div>tags</div>
    <div>comments</div>
  </div>
}