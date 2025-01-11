(ns linkhub.auth.frontend
  (:require [linkhub.frontend.store :as store]
            [clojure.core.async :as async]))

(defn init []
  {:store/state {::current-user [:result/not-asked]}})

(defmulti step store/event-type)

(defmethod step :default [i] i)

(defmethod step ::clicked-get-current-user [i] 
  (-> i
      (update-in [:store/state] assoc ::current-user [:result/loading])
      (update-in [:store/effect] conj [::get-current-user!])))

(defmethod step ::got-current-user [i] 
  (-> i
      (assoc-in [:store/state ::current-user] (-> i :store/event second))))


(defn get-current-user! []
  (async/go
    (async/<! (async/timeout 3000)) 
    [:result/ok
     {:user/user-id 1 
      :user/username "test-user" 
      :user/email "my-email"}]))

(defmethod store/effect! ::get-current-user! [i]
  (async/go
    (let [user (async/<! (get-current-user!))]
      ((:store/dispatch! i) [::got-current-user user]))))

(defmulti view-status (fn [i] (-> i :store/state ::current-user first)))

(defmethod view-status :result/not-asked [_]
  [:div "Not asked yet"])

(defmethod view-status :result/loading [_]
  [:div "Loading..."])

(defmethod view-status :result/ok [i]
  [:div (str "Current user: " (-> i :store/state ::current-user second :user/username))])

(defmethod view-status :result/err [_]
  [:div "Error!"])

(defn view-get-current-user-button [i]
  [:button {:on-click #((:store/dispatch! i) [::clicked-get-current-user])
            :disabled (-> i :store/state ::current-user first (= :result/loading))} "Get current user"])

(defn view [input]
  [:div 
   (view-get-current-user-button input)
   (view-status input)])

(store/register! {:store/init init 
                  :store/step step})