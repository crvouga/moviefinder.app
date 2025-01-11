(ns linkhub.frontend.counter
  (:require [linkhub.frontend.store :as store]))

(defn init []
  {:store/state {::count 0}})

(defmulti step store/event-type)

(defmethod step :default [i] i)

(defmethod step ::clicked-count-button [i] 
  (let [current-count (-> i :store/state ::count (or 0))
        output (assoc-in i [:store/state ::count] (inc current-count))]
    output))


(defn view [i]
  [:div
   "The state " [:code "click-count"] " has value: "
   (-> i :store/state ::count) ". "
   [:input {:type "button" :value "Click me!"
            :on-click #((:store/dispatch! i) [::clicked-count-button])}]])


(store/register! {:store/init init 
                  :store/step step})