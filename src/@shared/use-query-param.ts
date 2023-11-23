import { useEffect, useState } from "react";
import { z } from "zod";

export function useQueryParam<P extends z.ZodType<any, any, any>>({
  key,
  parser,
  initialValue,
}: {
  key: string;
  parser: P;
  initialValue: z.infer<P>;
}) {
  function getValue() {
    const url = new URL(window.location.href);
    const queryValue = url.searchParams.get(key);
    console.log("queryValue", queryValue);
    if (queryValue === null) {
      return initialValue;
    }

    try {
      const parsed = parser.parse(JSON.parse(atob(queryValue)));
      return parsed;
    } catch (error) {
      console.error("Error parsing query param:", error);
      return initialValue;
    }
  }

  const [value, setValue] = useState(getValue);

  useEffect(() => {
    const listener = () => {
      setValue(getValue());
    };

    window.addEventListener("popstate", listener);

    return () => {
      window.removeEventListener("popstate", listener);
    };
  }, []);

  function push(value: z.infer<P>) {
    const url = new URL(window.location.href);
    url.searchParams.set(key, btoa(JSON.stringify(value)));
    window.history.pushState(null, "", url.toString());
    window.dispatchEvent(new Event("popstate"));
  }

  function replace(value: z.infer<P>) {
    const url = new URL(window.location.href);
    url.searchParams.set(key, btoa(JSON.stringify(value)));
    window.history.replaceState(null, "", url.toString());
    window.dispatchEvent(new Event("popstate"));
  }

  return { value, push, replace } as const;
}
