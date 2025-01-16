(ns linkpage.frontend.counter
  (:require [linkpage.frontend.store :as store]
            [linkpage.frontend.routing :as routing]))

(defmulti transition store/msg-type)

(store/register-transition! transition)

(defmethod transition :default [i] i)

(defmethod transition :store/initialized [i]
  (-> i
      (update :store/state merge {::count 0})))

(defmethod transition ::clicked-count-button [i]
  (let [current-count (-> i :store/state ::count (or 0))
        output (assoc-in i [:store/state ::count] (inc current-count))]
    output))


(defn view [i]
  [:div
   [:button {:on-click #(store/put! i [:routing/clicked-link [:route/login]])} "Go to login"]
   "The state " [:code "click-count"] " has value: "
   (-> i :store/state ::count) ". "
   [:input {:type "button" :value "click me!"
            :on-click #(store/put! i [::clicked-count-button])}]])

(defmethod routing/view :route/counter [i]
  (view i))

