(ns app.auth.current-user.frontend
  (:require [clojure.core.async :as a]
            [core.program :as p]
            [app.frontend.ui.loading-state-logo :as loading-state-logo]
            [core.result :as result]))


(a/go-loop []
  (let [msg (a/<! (p/take! :login/authenticated))
        payload (second msg)
        current-user (-> (p/read!) ::current-user)
        current-user-new (if (result/ok? payload) payload current-user)
        _ (p/put! [::set-current-user current-user-new])]
    (recur)))


(a/go-loop []
  (let [msg (a/<! (p/take! ::got-current-user))
        payload (second msg)
        current-user (-> (p/read!) ::current-user)
        current-user-new (if (result/ok? payload) payload current-user)
        _ (p/put! [::set-current-user current-user-new])]
    (recur)))


(p/reg-reducer ::set-current-user (fn [state msg] (assoc state ::current-user (second msg))))

(defn loading? [i]
  (-> i  ::current-user :result/type (= :result/loading)))

(defn logged-in? [i]
  (let [current-user (-> i ::current-user)
        ok? (-> current-user :result/type (= :result/ok))
        user-id? (-> current-user :user/id nil? not)]
    (and ok? user-id?)))

(defn logged-out? [i]
  (and (not (loading? i))
       (-> i logged-in? not)))
(defmulti view-guard
  (fn [i _] (-> i ::current-user :result/type (or :result/not-asked))))

(defmethod view-guard :result/not-asked [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/loading [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/err [_ _]
  [loading-state-logo/view])

(defmethod view-guard :result/ok [i view-fn]
  [view-fn i])


