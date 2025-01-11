(ns linkhub.frontend.app
  (:require [reagent.dom.client :as rd]
            [linkhub.frontend.counter]
            [linkhub.auth.login.frontend]
            [linkhub.frontend.store :as store]
            [linkhub.frontend.routing :as routing]))

(defn init []
  {:store/state {}
   :store/events [[:app/initialized]]})

(defn view []
  (let [input {:store/state @store/state! 
               :store/dispatch! store/dispatch!}]
    (routing/view input)))

(store/register! {:store/init init})

(defn main [] 
  (store/init!)
  (let [dom-root (.getElementById js/document "root")
        react-root (rd/create-root dom-root)]
    (rd/render react-root [view])))
