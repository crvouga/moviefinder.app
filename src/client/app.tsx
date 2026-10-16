import { parseSearch, stringifySearch } from "../shared/search";
import { useEffect, lazy, Suspense } from "react";
import {
  createRootRoute,
  createRoute,
  createRouter,
  Link,
  Outlet,
  useLocation,
  useNavigate,
  RouterProvider,
} from "@tanstack/react-router";
import { Film } from "lucide-react";
import { authClient } from "./auth-client";
import { report } from "./notifications";
import { Toast } from "./components/toast";

const Navigation = lazy(() => import("./components/navigation"));
let stateModule: Promise<typeof import("./state")> | undefined;
const AuthPage = lazy(() => import("./pages/auth").then((m) => ({ default: m.AuthPage })));
const Discover = lazy(() => import("./pages/discover").then((m) => ({ default: m.Discover })));
const DetailPage = lazy(() => import("./pages/details").then((m) => ({ default: m.DetailPage })));
const SearchPage = lazy(() => import("./pages/search").then((m) => ({ default: m.SearchPage })));
const ListsPage = lazy(() => import("./pages/lists").then((m) => ({ default: m.ListsPage })));
const ListDetail = lazy(() => import("./pages/lists").then((m) => ({ default: m.ListDetail })));
const ActivityPage = lazy(() =>
  import("./pages/social").then((m) => ({ default: m.ActivityPage })),
);
const ProfilePage = lazy(() => import("./pages/social").then((m) => ({ default: m.ProfilePage })));
function Root() {
  const location = useLocation(),
    navigate = useNavigate(),
    session = authClient.useSession(),
    isAuth = location.pathname.startsWith("/auth");
  useEffect(() => {
    if (!isAuth && !session.isPending && !session.data)
      void navigate({ to: "/auth", search: { returnTo: location.href }, replace: true });
  }, [session.isPending, session.data, isAuth]);
  useEffect(() => {
    if (session.data && !isAuth) {
      stateModule ??= import("./state");
      void stateModule.then((m) => m.startSync(session.data!.user.id)).catch(report);
    }
    if (isAuth && stateModule) void stateModule.then((m) => m.stopSync());
  }, [session.data?.user.id, isAuth]);
  if (isAuth)
    return (
      <>
        <Suspense
          fallback={
            <div className="route-placeholder" aria-label="Opening this page">
              <div className="skeleton-line wide" />
              <div className="skeleton-line" />
            </div>
          }
        >
          <Outlet />
        </Suspense>
        <Toast />
      </>
    );
  if (!session.data)
    return (
      <main className="session-restoration">
        <span className="brand-icon">
          <Film size={25} />
        </span>
        <h1>moviefinder.</h1>
        <p>
          {session.isPending
            ? "Getting your seat ready."
            : "Your next good story starts with signing in."}
        </p>
      </main>
    );
  return (
    <div className="app-shell">
      <Suspense
        fallback={
          <div className="mobile-header">
            <span className="brand">moviefinder.</span>
          </div>
        }
      >
        <Navigation userId={session.data.user.id} />
      </Suspense>
      <main className="app-main" id="main-content">
        <Suspense
          fallback={
            <div className="route-placeholder" aria-label="Opening this page">
              <div className="skeleton-line wide" />
              <div className="skeleton-line" />
            </div>
          }
        >
          <Outlet />
        </Suspense>
      </main>
      <Toast />
    </div>
  );
}
function useCurrentUser() {
  return authClient.useSession().data?.user.id ?? "";
}
const root = createRootRoute({
  component: Root,
  validateSearch: (search: Record<string, unknown>) => search,
  notFoundComponent: () => (
    <div className="standard-page">
      <h1>A story for another day.</h1>
      <p>This page couldn’t be found.</p>
      <Link to="/">Back to Discover</Link>
    </div>
  ),
});
const home = createRoute({
  getParentRoute: () => root,
  path: "/",
  component: () => <Discover userId={useCurrentUser()} />,
});
const auth = createRoute({ getParentRoute: () => root, path: "/auth", component: AuthPage });
const search = createRoute({ getParentRoute: () => root, path: "/search", component: SearchPage });
const lists = createRoute({
  getParentRoute: () => root,
  path: "/lists",
  component: () => <ListsPage userId={useCurrentUser()} />,
});
const list = createRoute({
  getParentRoute: () => root,
  path: "/lists/$listId",
  component: () => <ListDetail userId={useCurrentUser()} />,
});
const detail = createRoute({
  getParentRoute: () => root,
  path: "/title/$id",
  component: () => <DetailPage userId={useCurrentUser()} />,
});
const activity = createRoute({
  getParentRoute: () => root,
  path: "/activity",
  component: () => <ActivityPage userId={useCurrentUser()} />,
});
const profile = createRoute({
  getParentRoute: () => root,
  path: "/profile",
  component: () => <ProfilePage currentUserId={useCurrentUser()} />,
});
const publicProfile = createRoute({
  getParentRoute: () => root,
  path: "/profile/$userId",
  component: () => <ProfilePage currentUserId={useCurrentUser()} />,
});
export const router = createRouter({
  parseSearch,
  stringifySearch,
  routeTree: root.addChildren([
    home,
    auth,
    search,
    lists,
    list,
    detail,
    activity,
    profile,
    publicProfile,
  ]),
  scrollRestoration: true,
  defaultPreload: "intent",
});
declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
export function App() {
  return <RouterProvider router={router} />;
}
