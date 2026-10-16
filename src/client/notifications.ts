export const notices = new EventTarget();
export function notify(message: string) {
  notices.dispatchEvent(new CustomEvent("notice", { detail: message }));
}
export function report(error: unknown) {
  notify(error instanceof Error ? error.message : "This action could not be completed");
}
