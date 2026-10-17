import { fieldText } from "../../shared/contracts";
import { useEffect, useState } from "react";
import { useLocation } from "@tanstack/react-router";
import { ArrowRight, Sparkles, Film, ArrowLeft } from "lucide-react";
import { authClient } from "../auth-client";
import { report } from "../notifications";
import { safeReturnTo } from "../../shared/contracts";
import { Button } from "../components/ui/button";
export function AuthPage() {
  const location = useLocation(),
    params = new URLSearchParams(location.searchStr),
    resetToken = params.get("token");
  const [mode, setMode] = useState<"login" | "register" | "recover" | "reset">(
      resetToken ? "reset" : "login",
    ),
    [busy, setBusy] = useState(false),
    [message, setMessage] = useState(""),
    [options, setOptions] = useState({ recovery: false, google: false });
  useEffect(() => {
    void fetch("/api/auth-options")
      .then((r) => r.json())
      .then(setOptions)
      .catch(() => {});
  }, []);
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    const data = new FormData(event.currentTarget),
      email = fieldText(data.get("email") ?? ""),
      password = fieldText(data.get("password") ?? "");
    try {
      const result =
        mode === "register"
          ? await authClient.signUp.email({
              email,
              password,
              name: fieldText(data.get("name")),
              username: fieldText(data.get("username")),
            })
          : mode === "recover"
            ? await authClient.requestPasswordReset({
                email,
                redirectTo: `${window.location.origin}/auth?mode=reset`,
              })
            : mode === "reset"
              ? await authClient.resetPassword({ newPassword: password, token: resetToken ?? "" })
              : await authClient.signIn.email({ email, password });
      if (result.error) throw new Error(result.error.message ?? "Please try again");
      if (mode === "recover")
        setMessage("If that email is registered, a recovery link is on its way.");
      else if (mode === "reset") {
        setMessage("Password updated. You can sign in now.");
        setMode("login");
      } else window.location.assign(safeReturnTo(params.get("returnTo")));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Please try again");
    } finally {
      setBusy(false);
    }
  }
  return (
    <main className="auth-screen">
      <section className="auth-art">
        <a href="/auth" className="brand">
          <span className="brand-icon">
            <Film size={19} />
          </span>
          moviefinder<span className="brand-dot">.</span>
        </a>
        <div className="auth-art-copy">
          <span className="eyebrow">FOR THE LOVE OF A GOOD STORY</span>
          <h1>
            Less scrolling.
            <br />
            More <em>cinema.</em>
          </h1>
          <p>
            The next film you love is out there.
            <br />
            Find it. Make a list. Bring your people.
          </p>
          <div className="auth-art-line" />
        </div>
        <span className="auth-note">A little curiosity. A great movie night.</span>
      </section>
      <section className="auth-form-wrap">
        <div className="auth-mobile-brand">
          <Film /> moviefinder.
        </div>
        <div className="auth-form">
          <span className="eyebrow">
            <Sparkles size={14} /> YOUR NEXT OBSESSION AWAITS
          </span>
          <h2>
            {mode === "register"
              ? "Good stories start here."
              : mode === "recover"
                ? "Let’s get you back in."
                : mode === "reset"
                  ? "A fresh start."
                  : "Welcome to your next watch."}
          </h2>
          <p>
            {mode === "register"
              ? "Join the people who always have a good recommendation."
              : mode === "recover"
                ? "We’ll email you a link to reset your password."
                : mode === "reset"
                  ? "Choose a password with at least 10 characters."
                  : "A world of films, series, and very good taste."}
          </p>
          <form onSubmit={submit}>
            {mode === "register" && (
              <>
                <label>
                  Display name
                  <input
                    name="name"
                    required
                    autoComplete="name"
                    maxLength={100}
                    placeholder="What should we call you?"
                  />
                </label>
                <label>
                  Username
                  <input
                    name="username"
                    required
                    autoComplete="username"
                    pattern="[a-zA-Z0-9_]{3,30}"
                    placeholder="Your public handle"
                  />
                </label>
              </>
            )}
            {mode !== "reset" && (
              <label>
                Email address
                <input
                  type="email"
                  name="email"
                  required
                  autoComplete="email"
                  placeholder="you@example.com"
                />
              </label>
            )}
            {mode !== "recover" && (
              <label>
                Password
                <input
                  type="password"
                  name="password"
                  required
                  minLength={10}
                  maxLength={128}
                  autoComplete={mode === "login" ? "current-password" : "new-password"}
                  placeholder="At least 10 characters"
                />
              </label>
            )}
            {message && (
              <div role="status" className="form-message">
                {message}
              </div>
            )}
            <Button disabled={busy} type="submit">
              {busy
                ? "One moment…"
                : mode === "register"
                  ? "Create your account"
                  : mode === "recover"
                    ? "Send recovery link"
                    : mode === "reset"
                      ? "Set new password"
                      : "Let’s find something good"}
              <ArrowRight size={18} />
            </Button>
          </form>
          {mode === "login" && options.recovery && (
            <button className="text-button recovery" onClick={() => setMode("recover")}>
              Forgot your password?
            </button>
          )}
          {mode === "login" && options.google && (
            <Button
              variant="outline"
              onClick={() =>
                void authClient.signIn
                  .social({ provider: "google", callbackURL: safeReturnTo(params.get("returnTo")) })
                  .catch(report)
              }
            >
              Continue with Google
            </Button>
          )}
          <div className="auth-switch">
            {mode === "login" ? (
              <>
                New around here?{" "}
                <button
                  onClick={() => {
                    setMode("register");
                    setMessage("");
                  }}
                >
                  Make yourself at home <ArrowRight size={14} />
                </button>
              </>
            ) : (
              <button
                onClick={() => {
                  setMode("login");
                  setMessage("");
                }}
              >
                <ArrowLeft size={14} /> Back to sign in
              </button>
            )}
          </div>
          <p className="auth-fine">Made for movie nights. Better with friends.</p>
        </div>
      </section>
    </main>
  );
}
