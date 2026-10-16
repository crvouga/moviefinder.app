import { fieldText } from "./contracts";
export function parseSearch(search: string): Record<string, string> {
  return Object.fromEntries(new URLSearchParams(search));
}
export function stringifySearch(search: Record<string, unknown>) {
  const params = new URLSearchParams();
  for (const [k, v] of Object.entries(search))
    if (v !== undefined && v !== null)
      params.set(k, Array.isArray(v) ? v.map(fieldText).join(",") : fieldText(v));
  const text = params.toString();
  return text ? `?${text}` : "";
}
