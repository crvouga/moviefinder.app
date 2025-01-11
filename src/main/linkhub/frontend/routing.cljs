(ns linkhub.frontend.routing 
  (:require
    [linkhub.frontend.store :as store]))

(defn init []
  {:store/state {::route [:route/login]}})

(defmulti step store/event-type)

(defmethod step :default [i] i)

(defmethod step :routing/clicked-link [i]
  (-> i
      (assoc-in [:store/state ::route] (store/event-payload i))))

(defmulti view (fn [i] (-> i :store/state ::route first)))

(store/register! {:store/init init :store/step step})