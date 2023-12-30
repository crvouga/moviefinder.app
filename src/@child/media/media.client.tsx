import { createClient } from "../../@shared/client"
import { contract } from "./media.contract"


const client = createClient(contract)

export const MediaPage = ({ mediaId }: { mediaId: string }) => {


    return <div>Media: {mediaId}</div>
}