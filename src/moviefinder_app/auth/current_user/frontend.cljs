(ns moviefinder-app.auth.current-user.frontend
  (:require [moviefinder-app.frontend.store :as store]
            [moviefinder-app.frontend.ui.loading-state-logo :as loading-state-logo]))

(store/register!
 :store/initialized
 (fn [i]
   (-> i
       (update-in [:store/state] assoc ::current-user {:result/type :result/loading})
       (update-in [:store/effs] conj [:rpc/send! {:rpc/req [:current-user/get]
                                                  :rpc/res #(vector ::got-current-user %)}])))

 :login/authenticated
 (fn [i]
   (let [current-user (-> i :store/state ::current-user)
         payload (store/msg-payload i)
         current-user-new (if (-> payload :result/type (= :result/ok)) payload current-user)]
     (-> i
         (assoc-in [:store/state ::current-user] current-user-new))))

 ::got-current-user
 (fn [i]
   (-> i
       (assoc-in [:store/state ::current-user] (-> i store/msg-payload (assoc :result/type :result/ok))))))

(defn loading? [i]
  (-> i :store/state ::current-user :result/type (= :result/loading)))

(defn logged-in? [i]
  (let [current-user (-> i :store/state ::current-user)
        ok? (-> current-user :result/type (= :result/ok))
        user-id? (-> current-user :user/id nil? not)]
    (and ok? user-id?)))

(defn logged-out? [i]
  (and (not (loading? i))
       (-> i logged-in? not)))
(defmulti view-guard
  (fn [i _] (-> i :store/state ::current-user :result/type (or :result/not-asked))))

(defmethod view-guard :result/not-asked [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/loading [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/err [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/ok [i view-fn]
  [view-fn i])


