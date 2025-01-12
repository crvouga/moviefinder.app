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
   :store/effects [[::get-route!]
                   [::subscribe-route!]]})

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(def fallback [:route/login])

(defn- get-route! []
  (-> js/window.location.pathname (subs 1) (decode) (or fallback)))

(defmethod store/effect! ::get-route! [i]
  (store/dispatch! i [::got-route (get-route!)]))

(defn assoc-msg-payload-as-route [i]
  (let [route-new (store/msg-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new))))

(defmethod step ::got-route [i]
  (assoc-msg-payload-as-route i))

(defmethod step ::route-changed [i]
  (assoc-msg-payload-as-route i))

(defmethod step :routing/clicked-link [i]
  (let [route-new (store/msg-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new)
        (update-in [:store/effects] conj [::push-route! route-new]))))

(defmethod store/effect! ::push-route! [i]
  (let [route (store/effect-payload i)
        encoded (encode route)]
    (js/window.history.pushState nil nil encoded)))

(defmethod store/effect! ::subscribe-route! [i]
  (doseq [event "popstate pushstate replacestate"]
    (js/window.addEventListener event #(store/dispatch! i [::route-changed (get-route!)]))))

(defmulti view (fn [i] (-> i :store/state ::route first)))

(store/register! {:store/init init :store/step step})