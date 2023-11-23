import { router } from "./@shared/trpc";


export const appRouter = router({
  // ...
});


export type AppRouter = typeof appRouter;
