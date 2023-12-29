export type Err<TError> = { t: "err"; error: TError };

export type Ok<TData> = { t: "ok"; data: TData };

export type Result<TData, TError> = Err<TError> | Ok<TData>;

export const Ok = <TData>(data: TData): Ok<TData> => ({ t: "ok", data });

export const Err = <TError>(error: TError): Err<TError> => ({ t: "err", error });

export const isErr = <TData, TError>(result: Result<TData, TError>): result is Err<TError> => result.t === "err";

export const isOk = <TData, TError>(result: Result<TData, TError>): result is Ok<TData> => result.t === "ok";

