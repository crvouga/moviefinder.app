(ns linkpage.frontend.routing
  (:require
   [linkpage.frontend.store :as store]
   [linkpage.frontend.route :as route]))

(defmulti transition store/msg-type)

(store/register-transition! transition)

(defmethod transition :default [i] i)

(defmethod transition :store/initialized [i]
  (-> i
      #_(update :store/state merge {::route [:route/login]})
      (update :store/effs conj [::get-route!])
      (update :store/effs conj [::subscribe-route!])))

(def fallback [:route/login])

(defn- get-route! []
  (or (route/get!) fallback))

(defmethod store/eff! ::get-route! [i]
  (store/put! i [::got-route (get-route!)]))

(defn assoc-msg-payload-as-route [i]
  (-> i
      (assoc-in [:store/state ::route] (store/msg-payload i))))

(defmethod transition ::got-route [i]
  (assoc-msg-payload-as-route i))

(defmethod transition ::route-changed [i]
  (assoc-msg-payload-as-route i))

(defmethod transition :routing/clicked-link [i]
  (let [route-new (store/msg-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new)
        (update-in [:store/effs] conj [::push! route-new]))))

(defmethod transition :routing/push [i]
  (let [route-new (store/msg-payload i)]
    (-> i
        (assoc-in [:store/state ::route] route-new)
        (update-in [:store/effs] conj [::push! route-new]))))

(defmethod store/eff! ::push! [i]
  (let [route (store/eff-payload i)
        encoded (route/encode route)]
    (js/window.history.pushState nil nil encoded)))

(defmethod store/eff! ::subscribe-route! [i]
  (doseq [event ["popstate" "pushstate" "replacestate"]]
    (js/window.addEventListener event #(store/put! i [::route-changed (get-route!)]))))

(defn route-type [i]
  (-> i :store/state ::route first))

(defn route-payload [i]
  (-> i :store/state ::route second))

(defmulti view route-type)

(defmethod view nil []
  [:div "Loading..."])

