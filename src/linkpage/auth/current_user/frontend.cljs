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

(defmulti view (fn [i] (-> i :store/state ::current-user first)))

(defmethod view :result/not-asked [_i]
  [:div "Not asked"])

(defmethod view :result/loading [_i]
  [:div "Loading current user..."])

(defmethod view :result/err [_i]
  [:div "Errored while loading current user"])

(defmethod view :result/ok [i view]
  [view i])

(store/register-transition! transition)

