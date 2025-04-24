(ns app.frontend
  (:require ["react-dom/client" :as rd]
            [app.auth.frontend]
            [app.frontend.db]
            [app.frontend.screen :as screen]
            [app.frontend.toaster :as toaster]
            [app.media.frontend]
            [app.home.frontend]
            [app.profile.frontend]
            [app.rpc.frontend]
            [clojure.core.async :refer [<! go-loop]]
            [core.program :as p]
            [reagent.core :as r]))


(defn view [input]
  (println "view")
  [:div {:class "fixed left-1/2 top-1/2 flex h-[100dvh] w-screen -translate-x-1/2 -translate-y-1/2 items-center justify-center overflow-hidden bg-black text-white"}
   [:div {:class "relative flex h-full max-h-[915px] w-full max-w-[520px] flex-col items-center justify-center overflow-hidden rounded min-[520px]:border border-neutral-500"}
    [toaster/view input]
    [screen/view input]]])

(defonce root
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/createRoot dom-root)]
    react-root))

(defn render! [input]
  (.render root (r/as-element [view input])))

(defn -main []
  (go-loop []
    (<! (p/take! :*))
    (render! (p/read!))
    (recur)))


