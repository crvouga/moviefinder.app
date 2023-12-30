import { useQuery } from "@tanstack/react-query"
import { createClient } from "../../@shared/client"
import { contract } from "./media.contract"

const client = createClient(contract)

export const MediaPage = ({ mediaId }: { mediaId: string }) => {

    const { data } = useQuery({
        queryKey: ["media", mediaId],
        queryFn: () => {
            return client.mediaPage({
                body: {
                    mediaId,
                    mediaType: "movie",
                }
            })
        }
    })

    if (data?.status !== 200) {
        return <div>loading...</div>
    }

    const media = data.body

    return <div>

        <h1 className="w-full text-xl font-bold h-full">
            {media.title}
        </h1>

        <img src={media.posterUrl} />
    </div>
}