(ns moviefinder-app.auth.current-user.frontend
  (:require [moviefinder-app.frontend.store :as store]
            [moviefinder-app.core.result :as result]
            [moviefinder-app.frontend.ui.loading-state-logo :as loading-state-logo]))

(store/reg!
 :store/initialized
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::current-user [:result/loading])
       (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:current-user/get]
                                                  :rpc/res #(vector ::got-current-user %)}])))

 :login/authenticated
 (fn [i]
   (let [current-user (-> i :store/state ::current-user)
         payload (store/msg-payload i)
         current-user-new (or (result/ok? payload) current-user)]
     (-> i
         (assoc-in [:store/state ::current-user] current-user-new))))

 ::got-current-user
 (fn [i]
   (-> i
       (assoc-in [:store/state ::current-user] (-> i store/msg-payload result/conform)))))

(defn logged-out? [i]
  (let [current-user (-> i :store/state ::current-user result/conform)
        payload (result/payload current-user)]
    (and (result/ok? current-user) (nil? payload))))
(defmulti view-guard
  (fn [i _] (-> i :store/state ::current-user result/conform first)))

(defmethod view-guard :result/not-asked [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/loading [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/err [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/ok [i view-fn]
  [view-fn i])


