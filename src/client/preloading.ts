import { feedParams, type Coverage } from "../shared/contracts";
import { discover, loadEntity, loadSocial, collections, getEntity } from "./state";
import { screenModules } from "./screen-modules";

function pause(signal: AbortSignal, ms: number) {
  return new Promise<void>((resolve, reject) => {
    const abort = () => {
      clearTimeout(timer);
      reject(signal.reason);
    };
    const timer = setTimeout(() => {
      signal.removeEventListener("abort", abort);
      resolve();
    }, ms);
    signal.addEventListener("abort", abort, { once: true });
    if (signal.aborted) abort();
  });
}
function localURL(href: string) {
  const url = new URL(href, window.location.origin);
  return url.origin === window.location.origin ? url : undefined;
}

// Route loaders await the first cache read; speculation continues until provider coverage arrives.
// Both paths merge into the same collections and join the same cancellable HTTP requests.
export async function prepareScreen(
  href: string,
  userId: string,
  signal: AbortSignal,
  speculative = false,
) {
  const url = localURL(href);
  if (!url || !userId || signal.aborted) return;
  const path = url.pathname.split("/").filter(Boolean).map(decodeURIComponent);
  let read: (() => Promise<string | null>) | undefined;
  let code: Promise<unknown>;
  let social: Promise<unknown> | undefined;
  switch (path[0]) {
    case undefined: {
      code = screenModules.discover();
      const parsed = feedParams.safeParse(Object.fromEntries(url.searchParams));
      if (parsed.success) read = () => discover(parsed.data, signal, speculative);
      break;
    }
    case "title":
      code = screenModules.details();
      if (path[1]) read = () => loadEntity(path[1]!, signal, speculative);
      break;
    case "lists":
      code = screenModules.lists();
      social = path[1]
        ? loadSocial("list", path[1], undefined, signal, speculative)
        : loadSocial("lists", "yours", undefined, signal, speculative);
      break;
    case "profile":
      code = screenModules.social();
      social = loadSocial("profile", path[1] ?? userId, undefined, signal, speculative);
      break;
    case "activity":
      code = screenModules.social();
      break;
    case "search": {
      code = screenModules.search();
      const q = url.searchParams.get("q")?.trim();
      if (q) {
        const parsed = feedParams.safeParse({ q });
        if (parsed.success) read = () => discover(parsed.data, signal, speculative);
        social = loadSocial("search", q, undefined, signal, speculative);
      }
      break;
    }
    default:
      return;
  }
  const [, id] = await Promise.all([code, read?.(), social]);
  if (!speculative || !read || !id) return;
  // Server hydration is durable/shared. Cancel only this browser's interest and HTTP reads.
  const deadline = Date.now() + 15_000;
  while (!signal.aborted && Date.now() < deadline) {
    const coverage = collections.query.get(id)?.data.coverage as Coverage | undefined;
    if (!coverage || coverage.state === "complete" || coverage.state === "error") break;
    await pause(signal, 750);
    await read();
  }
}

function warmImages(href: string, signal: AbortSignal) {
  const url = localURL(href);
  if (!url?.pathname.startsWith("/title/")) return;
  const entity = getEntity(decodeURIComponent(url.pathname.slice("/title/".length)));
  if (!entity) return;
  const images: HTMLImageElement[] = [];
  for (const [path, size] of [
    [entity.poster, "w500"],
    [entity.backdrop, "w1280"],
  ] as const) {
    if (!path) continue;
    const image = new Image();
    image.fetchPriority = "low";
    if (size === "w500" && !path.startsWith("https://")) {
      image.sizes = "(max-width: 600px) 90vw, 420px";
      image.srcset = ["w342", "w500", "w780"]
        .map((s) => `https://image.tmdb.org/t/p/${s}${path} ${s.slice(1)}w`)
        .join(", ");
    }
    image.src = path.startsWith("https://") ? path : `https://image.tmdb.org/t/p/${size}${path}`;
    images.push(image);
  }
  const cancel = () =>
    images.forEach((image) => {
      image.removeAttribute("srcset");
      image.removeAttribute("src");
    });
  signal.addEventListener("abort", cancel, { once: true });
  if (signal.aborted) cancel();
}

export function preloadTitles(ids: string[], userId: string) {
  const controller = new AbortController();
  for (const id of ids) {
    const href = `/title/${encodeURIComponent(id)}`;
    warmImages(href, controller.signal);
    void prepareScreen(href, userId, controller.signal, true)
      .then(() => {
        if (!controller.signal.aborted) warmImages(href, controller.signal);
      })
      .catch(() => {});
  }
  return () => controller.abort();
}

// One session-scoped owner for intent, viewport predictions, and their cancellation.
export function startNavigationPreloading(userId: string) {
  const predictions = new Map<string, AbortController>();
  const intents = new Map<
    HTMLAnchorElement,
    { controller: AbortController; timer?: ReturnType<typeof setTimeout> }
  >();
  const visible = new Set<HTMLAnchorElement>();
  const observed = new Set<HTMLAnchorElement>();
  let refreshTimer: ReturnType<typeof setTimeout> | undefined;
  const connection = (
    navigator as Navigator & { connection?: { saveData?: boolean; effectiveType?: string } }
  ).connection;
  const constrained = connection?.saveData || /(^|-)2g$/.test(connection?.effectiveType ?? "");
  const anchor = (target: EventTarget | null) =>
    target instanceof Element ? target.closest<HTMLAnchorElement>("a[href]") : null;
  const eligible = (link: HTMLAnchorElement) => {
    const url = localURL(link.href);
    return (
      url &&
      !link.download &&
      (!link.target || link.target === "_self") &&
      !link.hasAttribute("data-no-preload") &&
      url.pathname !== "/auth" &&
      /^\/(?:$|title\/|lists(?:\/|$)|profile(?:\/|$)|activity$|search$)/.test(url.pathname) &&
      url.pathname + url.search !== location.pathname + location.search
    );
  };
  const launch = (href: string, controller: AbortController) => {
    warmImages(href, controller.signal);
    void prepareScreen(href, userId, controller.signal, true)
      .then(() => {
        if (!controller.signal.aborted) warmImages(href, controller.signal);
      })
      .catch(() => {
        /* Speculative errors must not interrupt the current screen. Navigation retries. */
      });
  };
  const releaseIntent = (link: HTMLAnchorElement) => {
    const intent = intents.get(link);
    if (!intent) return;
    clearTimeout(intent.timer);
    // A brief gap permits leave -> pointerdown/click to transfer ownership without restarting.
    intent.timer = setTimeout(() => {
      intent.controller.abort();
      intents.delete(link);
    }, 120);
  };
  const enter = (event: Event) => {
    if (
      document.hidden ||
      (event instanceof MouseEvent &&
        (event.ctrlKey || event.metaKey || event.altKey || event.shiftKey))
    )
      return;
    if (event instanceof PointerEvent && event.type === "pointerdown" && event.button !== 0) return;
    const link = anchor(event.target);
    if (!link || !eligible(link)) return;
    const existing = intents.get(link);
    if (existing) {
      clearTimeout(existing.timer);
      return;
    }
    const controller = new AbortController();
    intents.set(link, { controller });
    launch(link.href, controller);
    // Touch/press intent has no reliable leave event. Never retain it indefinitely.
    intents.get(link)!.timer = setTimeout(() => {
      controller.abort();
      intents.delete(link);
    }, 20_000);
  };
  const leave = (event: Event) => {
    const link = anchor(event.target);
    if (!link) return;
    const related = (event as MouseEvent | FocusEvent).relatedTarget;
    if (related instanceof Node && link.contains(related)) return;
    if (link.contains(document.activeElement) && event.type === "pointerout") return;
    releaseIntent(link);
  };
  const reconcile = () => {
    refreshTimer = undefined;
    const candidates =
      document.hidden || constrained
        ? []
        : [...visible]
            .filter(
              (link) =>
                link.isConnected &&
                eligible(link) &&
                !link.closest(".swiper-slide:not(.swiper-slide-active)"),
            )
            .sort((a, b) => {
              const rank = (link: HTMLAnchorElement) =>
                Number(!!link.closest("main")) * 10 +
                (link.pathname.startsWith("/title/")
                  ? 3
                  : link.pathname.startsWith("/lists/") || link.pathname.startsWith("/profile/")
                    ? 2
                    : 0);
              return rank(b) - rank(a);
            });
    const wanted = new Set(
      candidates
        .map((link) => link.href)
        .filter((href, i, all) => all.indexOf(href) === i)
        .slice(0, 4),
    );
    for (const [href, controller] of predictions)
      if (!wanted.has(href)) {
        controller.abort();
        predictions.delete(href);
      }
    for (const href of wanted)
      if (!predictions.has(href)) {
        const controller = new AbortController();
        predictions.set(href, controller);
        launch(href, controller);
      }
  };
  const schedule = () => {
    if (!refreshTimer) refreshTimer = setTimeout(reconcile, 80);
  };
  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        const link = entry.target as HTMLAnchorElement;
        if (entry.isIntersecting) visible.add(link);
        else visible.delete(link);
      }
      schedule();
    },
    { threshold: 0.15 },
  );
  const scan = () => {
    for (const link of observed)
      if (!link.isConnected) {
        observer.unobserve(link);
        observed.delete(link);
        visible.delete(link);
        intents.get(link)?.controller.abort();
        intents.delete(link);
      }
    for (const link of document.querySelectorAll<HTMLAnchorElement>("a[href]"))
      if (!observed.has(link)) {
        observed.add(link);
        observer.observe(link);
      }
    schedule();
  };
  const mutations = new MutationObserver(scan);
  mutations.observe(document.body, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ["href", "class"],
  });
  const events = ["pointerover", "pointerdown", "focusin"] as const;
  events.forEach((type) => document.addEventListener(type, enter, true));
  document.addEventListener("pointerout", leave, true);
  document.addEventListener("focusout", leave, true);
  const visibility = () => {
    if (document.hidden) {
      for (const intent of intents.values()) {
        clearTimeout(intent.timer);
        intent.controller.abort();
      }
      intents.clear();
    }
    schedule();
  };
  document.addEventListener("visibilitychange", visibility);
  scan();
  return () => {
    clearTimeout(refreshTimer);
    observer.disconnect();
    mutations.disconnect();
    for (const controller of predictions.values()) controller.abort();
    for (const intent of intents.values()) {
      clearTimeout(intent.timer);
      intent.controller.abort();
    }
    events.forEach((type) => document.removeEventListener(type, enter, true));
    document.removeEventListener("pointerout", leave, true);
    document.removeEventListener("focusout", leave, true);
    document.removeEventListener("visibilitychange", visibility);
  };
}
