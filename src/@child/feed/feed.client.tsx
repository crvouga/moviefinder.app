import { createClient, createQueryClient } from "../../@shared/client";
import { contract } from "./feed.contract";



const client_ = createClient(contract)


export const FeedPage = () => {
  return <div>Feed page</div>
}