import { useQuery } from "@tanstack/react-query";
import { initClient } from "@ts-rest/core";
import { contract } from "./feed.contract";

const client = initClient(contract, {
  baseUrl: "http://localhost:3000",
  baseHeaders: {}
}) 

export const FeedPage = () => {
  const { status, data } = useQuery({
    queryKey: ["feed"],
    queryFn: () => client.feed(),
  })

  if(data?.status === 200) {
    return <div>{data.body.map(x => x.body)}</div>
  }

  switch(status) {
    case 'error': {
      return <div>error</div>
    }
    case 'pending': {
      return <div>loading</div>
    }
    case 'success': {
      
      return <div>success</div>
    }
  }
}