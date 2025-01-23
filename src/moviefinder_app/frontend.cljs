(ns moviefinder-app.frontend
  (:require [reagent.dom.client :as rd]
            [moviefinder-app.auth.frontend]
            [moviefinder-app.auth.current-user.frontend :as current-user]
            [moviefinder-app.rpc.frontend]
            [moviefinder-app.frontend.runtime]
            [moviefinder-app.frontend.toaster :as toaster]
            [moviefinder-app.frontend.store :as store]
            [moviefinder-app.frontend.screen :as screen]))

(defn view-root [i]
  [:div {:class "w-[100dvw] h-[100dvh] flex flex-col items-center justify-start"}
   (toaster/view i)
   (current-user/view-guard i screen/view)])

(defn view []
  (store/view view-root))

(defn -main []
  (store/initialize!)
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
