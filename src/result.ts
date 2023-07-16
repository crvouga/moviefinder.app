export type Err<T> = { t: "err"; error: T };
export type Ok<T> = { t: "ok"; value: T };
export type Result<E, V> = Err<E> | Ok<V>;
export const Ok = <V>(value: V): Ok<V> => ({ t: "ok", value });
export const Err = <E>(error: E): Err<E> => ({ t: "err", error });
