(ns linkpage.frontend.counter
  (:require [linkpage.frontend.store :as store]
            [linkpage.frontend.routing :as routing]))

(defn init []
  {:store/state {::count 0}})

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(defmethod step ::clicked-count-button [i]
  (let [current-count (-> i :store/state ::count (or 0))
        output (assoc-in i [:store/state ::count] (inc current-count))]
    output))


(defn view [i]
  [:div
   [:button {:on-click #(store/dispatch! i [:routing/clicked-link [:route/login]])} "Go to login"]
   "The state " [:code "click-count"] " has value: "
   (-> i :store/state ::count) ". "
   [:input {:type "button" :value "click me!"
            :on-click #(store/dispatch! i [::clicked-count-button])}]])

(defmethod routing/view :route/counter [i]
  (view i))

(store/register! {:store/init init
                  :store/step step})