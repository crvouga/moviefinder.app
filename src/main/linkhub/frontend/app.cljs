(ns linkhub.frontend.app
  (:require [reagent.dom.client :as rd]
            [linkhub.frontend.counter :as counter]
            [linkhub.frontend.auth.client :as auth]
            [linkhub.frontend.store :as store]))


(defn view []
  (let [input {:store/state @store/state! 
               :store/dispatch! store/dispatch!}]
    [:<>
     [counter/view input]
     [auth/view input]]))

(defn init [] 
  (store/init!)
  (store/dispatch! [:app/initialized])
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
