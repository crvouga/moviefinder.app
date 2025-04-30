(ns app.auth.current-user.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]))

(defn set-current-user [state [_ current-user]]
  (assoc state ::current-user current-user))

(defn- logic [i]
  (p/reg-reducer i ::set-current-user set-current-user)

  (p/take-every!
   i :current-user/load
   (fn [_]
     (a/go
       (p/put! i [::set-current-user result/loading])
       (let [got-current-user (a/<! (p/eff! i [:rpc/send! [:rpc/get-current-user]]))]
         (p/put! i [::set-current-user got-current-user]))))))


(defn loading? [i]
  (-> i  ::current-user result/loading?))

(defn logged-in? [i]
  (let [current-user (-> i ::current-user)
        ok? (-> current-user result/ok?)
        user-id? (-> current-user :user/user-id nil? not)]
    (and ok? user-id?)))

(defn logged-out? [i]
  (and (not (loading? i))
       (-> i logged-in? not)))

(mod/reg
 {:mod/name :mod/current-user
  :mod/logic-fn logic})