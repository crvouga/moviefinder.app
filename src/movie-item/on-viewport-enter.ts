// onViewportEnter.ts
interface Options {
  once?: boolean;
}

export function onViewportEnter(
  node: HTMLElement,
  { once = true }: Options = {}
): any {
  let observer: IntersectionObserver | undefined;

  function startObserver() {
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          node.dispatchEvent(new CustomEvent("viewportenter"));
          if (once) {
            observer?.disconnect();
            observer = undefined;
          }
        }
      });
    });

    observer.observe(node);
  }

  startObserver();

  return {
    update(options: Options) {
      if (observer) {
        observer.disconnect();
        observer = undefined;
      }
      startObserver();
    },
    destroy() {
      if (observer) {
        observer.disconnect();
        observer = undefined;
      }
    },
  };
}
