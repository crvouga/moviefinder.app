(ns app.frontend
  (:require
   ["react-dom/client" :as rd]
   [app.auth.frontend]
   [app.frontend.db]
   [app.frontend.mod :as mod]
   [app.frontend.toaster]
   [app.home.frontend]
   [app.media.frontend]
   [app.profile.frontend]
   [app.rpc.frontend]
   [lib.js-obj :as js-obj]
   [lib.program :as p]
   [reagent.core :as r]
   [clojure.pprint :as pprint]))


(defonce ^:private program (p/new))

(defn view [i]
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    #_[:code (pr-str (screen/screen-name i))]
    [mod/view i]]])

(defonce root
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/createRoot dom-root)]
    react-root))

(defn render! [input]
  (try
    (.render root (r/as-element [view input]))
    (catch :default e
      (pprint/pprint {:render!/exception {:msg input
                                          :error e}}))))

(defn set-window-state! [state]
  (set! (.-appState js/window) (js-obj/init state)))

(defn -main []
  (p/take-every!
   program :*
   (fn []
     (let [state (p/state! program)
           input (merge state program)]
       (set-window-state! state)
       (render! input))))

  (mod/logic program))