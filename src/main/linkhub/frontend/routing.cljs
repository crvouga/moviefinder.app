(ns linkhub.frontend.routing 
  (:require
   [linkhub.frontend.store :as store]
   [cljs.reader :refer [read-string]]))

(defn- encode [route]
  (-> route pr-str js/btoa))

(defn- decode [route]
  (-> route js/atob  read-string))

(defn init []
  {:store/state {::route [:route/login]}
   :store/effects [[::get-route!]]})

(defmulti step store/event-type)

(defmethod step :default [i] i)

(defmethod store/effect! ::get-route! [i]
  (let [route (-> js/window.location.pathname (subs 1) (decode))]
    ((:store/dispatch! i) [::got-route route])))

(defmethod step ::got-route [i]
  (-> i
      (assoc-in [:store/state ::route] (store/event-payload i))))

(defmethod step :routing/clicked-link [i]
  (let [route-new (store/event-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new)
        (update-in [:store/effects] conj [::push-route! route-new]))))


(defmethod store/effect! ::push-route! [input]
  (let [route (-> input :store/effects second)
        encoded (encode route)]
    (js/window.history.pushState nil nil encoded)))

(defmulti view (fn [i] (-> i :store/state ::route first)))


(store/register! {:store/init init :store/step step})