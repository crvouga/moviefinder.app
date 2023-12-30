import { useEffect, useState } from "react";
import { z } from "zod";

export type QueryParamState<TValue> = {
  value: TValue;
  push: (value: TValue) => void;
  replace: (value: TValue) => void;
};

export function useQueryParamState<P extends z.ZodType<any, any, any>>({
  key,
  parser,
  initialValue,
}: {
  key: string;
  parser: P;
  initialValue: z.infer<P>;
}): QueryParamState<z.infer<P>> {
  function getValue(): z.infer<P> | null {
    const url = new URL(window.location.href);
    const queryValue = url.searchParams.get(key);
    
    if (queryValue === null) {
      return null;
    }

    try {
      const parsed = parser.parse(JSON.parse(atob(queryValue)));
      return parsed;
    } catch (error) {      
      return null;
    }
  }
  const getValueWithFallback = () => getValue() ?? initialValue;

  const [value, setValue] = useState(getValueWithFallback);

  useEffect(() => {
    if (getValue() === null) {
      replace(initialValue);
    }

    const onPopState = () => {      
      setValue(getValueWithFallback());
    }

    const onPushState = () => {      
      setValue(getValueWithFallback());
    };

    window.addEventListener("popstate", onPopState);
    window.addEventListener("pushstate", onPushState);

    return () => {
      window.removeEventListener("popstate", onPopState);
      window.removeEventListener("pushstate", onPushState);
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

  return { value, push, replace }
}
