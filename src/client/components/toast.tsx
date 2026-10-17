import { useEffect, useState } from "react";
import { notices } from "../notifications";
export function Toast() {
  const [message, setMessage] = useState("");
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>;
    const show = (e: Event) => {
      setMessage((e as CustomEvent<string>).detail);
      clearTimeout(timer);
      timer = setTimeout(() => setMessage(""), 6000);
    };
    notices.addEventListener("notice", show);
    return () => {
      notices.removeEventListener("notice", show);
      clearTimeout(timer);
    };
  }, []);
  return message ? (
    <div role="status" className="toast" onClick={() => setMessage("")}>
      {message}
    </div>
  ) : null;
}
