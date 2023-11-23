import { publicProcedure, router } from "../../@shared/trpc";


export const feedRouter = router({
    getFeed: publicProcedure.query(() => {
        return {
            message: "hello"
        }
    })

})



export type FeedRouter = typeof feedRouter;
