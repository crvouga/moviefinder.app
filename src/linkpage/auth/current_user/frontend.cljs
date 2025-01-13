(ns linkpage.auth.current-user.frontend
  (:require [linkpage.frontend.store :as store]))

(defmulti step store/msg-type)

(defmethod step :default [i] i)

(defn eff-load-current-user [_i]
  [:rpc/send! {:rpc/req [:current-user/get]
               :rpc/res #(vector ::got-current-user %)}])

(defn step-load-current-user [i]
  (-> i
      (update-in [:store/state] assoc ::current-user [:result/loading])
      (update-in [:store/effs] conj (eff-load-current-user i))))

(defmethod step :store/initialized [i]
  (-> i
      (assoc-in [:store/state ::current-user] [:result/not-asked])
      (assoc-in [:store/state ::send-code] [:result/not-asked])
      step-load-current-user))

(defmethod step ::got-current-user [i]
  (-> i
      (assoc-in [:store/state ::current-user] (store/msg-payload i))))

(defmulti view (fn [i] (-> i :store/state ::current-user first)))

(defmethod view :result/not-asked [_i]
  [:div "Not asked"])

(defmethod view :result/loading [_i]
  [:div "Loading current user..."])

(defmethod view :result/err [_i _view-logged-out _view-logged-in]
  [:div "Errored while loading current user"])

(defmethod view :result/ok [i]
  (let [current-user (-> i :store/state ::current-user second)
        view-logged-in (-> i :current-user/view-logged-in)
        view-logged-out (-> i :current-user/view-logged-out)]
    (if current-user
      [view-logged-in i]
      [view-logged-out i])))

(store/register-step! step)

