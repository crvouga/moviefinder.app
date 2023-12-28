import { useQuery } from "@tanstack/react-query";
import { contract } from "./feed.contract";
import { createClient } from "../../@shared/client";

const client = createClient(contract) 

export const FeedPage = () => {
  const { data } = useQuery({
    queryKey: ["feed"],
    queryFn: () => client.feed(),
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
        <div key={item.id}>
          <div>{item.title}</div>
        </div>
      )
    })}
  </div>
}