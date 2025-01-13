(ns linkpage.frontend.routing
  (:require
   [linkpage.frontend.store :as store]
   [linkpage.frontend.route :as route]))

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(defmethod step :store/initialized [i]
  (-> i
      (update :store/state merge {::route [:route/login]})
      (update :store/effs conj [::get-route!])
      (update :store/effs conj [::subscribe-route!])))

(def fallback [:route/login])

(defn- get-route! []
  (or (route/get!) fallback))

(defmethod store/eff! ::get-route! [i]
  (store/dispatch! i [::got-route (get-route!)]))

(defn assoc-msg-payload-as-route [i]
  (-> i
      (assoc-in [:store/state ::route] (store/msg-payload i))))

(defmethod step ::got-route [i]
  (assoc-msg-payload-as-route i))

(defmethod step ::route-changed [i]
  (assoc-msg-payload-as-route i))

(defmethod step :routing/clicked-link [i]
  (let [route-new (store/msg-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new)
        (update-in [:store/effs] conj [::push-route! route-new]))))

(defmethod store/eff! ::push-route! [i]
  (let [route (store/eff-payload i)
        encoded (route/encode route)]
    (js/window.history.pushState nil nil encoded)))

(defmethod store/eff! ::subscribe-route! [i]
  (doseq [event "popstate pushstate replacestate"]
    (js/window.addEventListener event #(store/dispatch! i [::route-changed (get-route!)]))))

(defmulti view (fn [i] (-> i :store/state ::route first)))



(store/register-step! step)