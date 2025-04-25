(ns app.auth.current-user.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [core.program :as p]
   [core.result :as result]))

(defn- logic [i]
  (p/reg-reducer i ::set-current-user (fn [state msg] (assoc state ::current-user (second msg))))

  (a/go-loop []
    (let [msg (a/<! (p/take! i :login/authenticated))
          payload (second msg)
          state (p/state! i)
          current-user (-> state ::current-user)
          current-user-new (if (result/ok? payload) payload current-user)]
      (p/put! i [::set-current-user current-user-new])
      (recur))))


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


(mod/reg
 {:mod/name :mod/current-user
  :mod/logic-fn logic})