// The same module promises serve speculative loading and React.lazy rendering.
function once<T>(load: () => Promise<T>) {
  let pending: Promise<T> | undefined;
  return () =>
    (pending ??= load().catch((error) => {
      pending = undefined;
      throw error;
    }));
}
export const screenModules = {
  discover: once(() => import("./pages/discover")),
  details: once(() => import("./pages/details")),
  search: once(() => import("./pages/search")),
  lists: once(() => import("./pages/lists")),
  social: once(() => import("./pages/social")),
};
