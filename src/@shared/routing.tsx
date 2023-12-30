import { z } from "zod";
import { QueryParamState, useQueryParamState } from "./use-query-param";
import { createContext, useContext } from "react";

// 
// 
// 
// 
// 
// 
// 


export const Route = z.discriminatedUnion("t", [
  z.object({
    t: z.literal("feed"),
    activeIndex: z.number().int().nonnegative(),
  }),
  z.object({
    t: z.literal("media-details"),
    from: z.enum(['feed']),
    mediaId: z.string(),
  }),
])

export type Route = z.infer<typeof Route>

// 
// 
// 
// 
// 
// 
// 

const RouteContext = createContext<QueryParamState<Route> | null>(null)

export const RouteProvider = ({ children }: { children: React.ReactNode }) => {
  const route = useQueryParamState({
    key: "route",
    parser: Route,
    initialValue: {
      t: "feed",
      activeIndex: 0,
    }
  })

  return (
    <RouteContext.Provider value={route}>
      {children}
    </RouteContext.Provider>
  )
}


export const useRoute = () => {
  const context = useContext(RouteContext)

  if (context === null) {
    throw new Error("useRoute must be used inside RouteProvider")
  }

  return context
}
