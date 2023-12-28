import { useQueryParam } from "./use-query-param";
import { z } from "zod";

export const Page = z.discriminatedUnion("t", [
  z.object({
    t: z.literal("feed"),
  }),
  z.object({
    t: z.literal("movie-details"),
  }),
]);

export type Page = z.infer<typeof Page>;

export function useCurrentPage() {
  const currentPage = useQueryParam({
    key: "currentPage",
    parser: Page,
    initialValue: {
      t: "feed",
    },
  });

  return currentPage;
}
