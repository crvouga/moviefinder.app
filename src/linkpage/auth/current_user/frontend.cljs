(ns linkpage.auth.current-user.frontend
  (:require [linkpage.frontend.store :as store]))

(defmulti transition store/msg-type)

(defmethod transition :default [i] i)

(defmethod transition :store/initialized [i]
  (-> i
      (update-in [:store/state] assoc ::current-user [:result/loading])
      (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:current-user/get]
                                                 :rpc/res #(vector ::got-current-user %)}])))

(defmethod transition ::got-current-user [i]
  (-> i
      (assoc-in [:store/state ::current-user] (store/msg-payload i))))

#_(defmethod transition :login/authenticated [i]
    (-> i
        (assoc-in [:store/state ::current-user] (store/msg-payload i))))

(defmulti view (fn [i] (-> i :store/state ::current-user first)))

(defn view-centered-message [message]
  [:div {:style {:display "flex"
                 :justify-content "center"
                 :align-items "center"
                 :width "100vw"
                 :height "100vh"}}
   [:strong message]])

(defmethod view :result/not-asked [_i]
  [view-centered-message "Not asked"])

(defmethod view :result/loading [_i]
  [view-centered-message "Loading current user..."])

(defmethod view :result/err [_i]
  [view-centered-message "Errored while loading current user"])

(defmethod view :result/ok [i view]
  [view i])

(store/register-transition! transition)

