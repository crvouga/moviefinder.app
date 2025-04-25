(ns app.frontend
  (:require
   ["react-dom/client" :as rd]
   [app.auth.frontend]
   [app.frontend.db]
   [app.frontend.mod :as mod]
   [app.frontend.screen]
   [app.frontend.toaster]
   [app.home.frontend]
   [app.media.frontend]
   [app.profile.frontend]
   [app.rpc.frontend]
   [clojure.core.async :as a]
   [core.js-obj :as js-obj]
   [core.program :as p]
   [reagent.core :as r]))


(defonce ^:private program (p/new))

(defn view [i]
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    [mod/view i]]])

(defonce root
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/createRoot dom-root)]
    react-root))

(defn render! [input]
  (.render root (r/as-element [view input])))



(defn -main []
  (mod/logic program)

  (a/go-loop []
    (a/<! (p/take! program :*))
    (let [state (p/state! program)
          input (merge state program)]
      (set! (.-appState js/window) (js-obj/init state))

      (render! input))
    (recur)))


