(ns linkhub.frontend.counter
  (:require [linkhub.frontend.store :as store]))

(defn init []
  {:store/state {::count 0}})

(defmulti step (fn [input] (-> input :store/event first)))

(defmethod step ::clicked-count-button [input] 
  (let [current-count (-> input :store/state ::count (or 0))
        output (assoc-in input [:store/state ::count] (inc current-count))]
    output))


(defn view [input dispatch!]
  [:div
   "The state " [:code "click-count"] " has value: "
   (::count input) ". "
   [:input {:type "button" :value "Click me!"
            :on-click #(dispatch! [::clicked-count-button])}]])


(store/register! {:store/init init 
                  :store/step step})