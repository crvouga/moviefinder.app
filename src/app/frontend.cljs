(ns app.frontend
  (:require
   ["react-dom/client" :as rd]
   [app.auth.frontend]
   [app.frontend.ctx :refer [ctx]]
   [app.frontend.db]
   [app.frontend.mod :as mod]
   [app.frontend.toaster]
   [app.home.frontend]
   [app.media.frontend]
   [app.profile.frontend]
   [app.rpc.frontend]
   [lib.js-obj :as js-obj]
   [lib.program :as p]
   [reagent.core :as r]))

(defn view [i]
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    #_[:code (pr-str (screen/screen-name i))]
    [mod/view i]]])

(defonce p (p/new))

(defonce react-root (rd/createRoot (.getElementById js/document "root")))

(defn render! [i]
  (.render react-root (r/as-element [view i])))

(defn set-window-state! [i]
  (set! (.-appState js/window) (js-obj/init i)))

(defn -main []
  (mod/logic (merge ctx p))
  (p/take-every! p :* (fn [] (-> p p/state! set-window-state!)))
  (p/take-every! p :* (fn [] (-> p p/state! (merge p) render!))))