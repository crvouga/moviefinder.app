(ns linkhub.frontend.app
  (:require [reagent.dom.client :as rd]
            [linkhub.frontend.counter :as counter]
            [linkhub.frontend.store :as store]))


(defn view []
  (let [state @store/state! 
        dispatch! store/dispatch!]
    [counter/view state dispatch!]))

(defn init [] 
  (store/init!)
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
