(ns linkpage.auth.current-user.frontend
  (:require [linkpage.frontend.store :as store]
            [linkpage.core.result :as result]))

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
       (assoc-in [:store/state ::current-user] (store/msg-payload i)))))

(defn view-centered-message [message]
  [:div {:style {:display "flex"
                 :justify-content "center"
                 :align-items "center"
                 :width "100vw"
                 :height "100vh"}}
   [:strong message]])

(defmulti view-guard
  (fn [i] (-> i :store/state ::current-user result/payload)))

(defmethod view-guard :result/not-asked [_i]
  [view-centered-message "Not asked"])

(defmethod view-guard :result/loading [_i]
  [view-centered-message "Loading current user..."])

(defmethod view-guard :result/err [_i]
  [view-centered-message "Errored while loading current user"])

(defmethod view-guard :result/ok [i view]
  [view i])


