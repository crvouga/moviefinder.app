(ns app.auth.current-user.frontend
  (:require
   [app.frontend.mod :as mod]
   [core.program :as p]
   [core.result :as result]))

(defn- logic [i]
  (p/reg-reducer i ::set-current-user (fn [state msg] (assoc state ::current-user (second msg))))

  (p/take-every!
   i :current-user/load
   (fn [_]
     (let [got-current-user (p/eff! i [:rpc/send! [:rpc/get-current-user]])]
       (p/put! i [::set-current-user got-current-user])))))


(defn loading? [i]
  (-> i  ::current-user result/loading?))

(defn logged-in? [i]
  (let [current-user (-> i ::current-user)
        ok? (-> current-user result/ok?)
        user-id? (-> current-user :user/id nil? not)]
    (and ok? user-id?)))

(defn logged-out? [i]
  (and (not (loading? i))
       (-> i logged-in? not)))

(mod/reg
 {:mod/name :mod/current-user
  :mod/logic-fn logic})