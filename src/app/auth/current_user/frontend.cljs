(ns app.auth.current-user.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]
   [app.frontend.db :as db]))


(defn loading? [i]
  (-> i  ::request result/loading?))

(defn logged-in? [i]
  (let [current-user (-> i ::request)
        ok? (-> current-user result/ok?)
        user-id? (-> current-user :user/user-id nil? not)]
    (and ok? user-id?)))

(defn logged-out? [i] (and (not (loading? i)) (-> i logged-in? not)))

(defn to-status [i]
  (cond
    (logged-in? i) :current-user/logged-in
    (logged-out? i) :current-user/logged-out
    :else :current-user/loading))

(defn to-current-user-id [i] (-> i ::request :user/user-id))

(defn to-current-user [i] (db/to-entity i (to-current-user-id i)))

(defn- logic [i]
  (p/reg-reducer i ::set (fn [s [_ k v]] (assoc s k v)))

  (p/take-every!
   i :current-user/got
   (fn [[_ current-user]]
     (p/put! i [:db/got-entity (:user/user-id current-user) current-user])
     (p/put! i [::set ::request (merge current-user result/ok)])))

  (p/reg-reducer i :current-user/dissoc (fn [s _] (assoc s ::request nil)))


  (a/go
    (p/put! i [::load]))

  (p/take-every!
   i ::load
   (fn []
     (a/go
       (p/put! i [::set ::request result/loading])
       (let [got (a/<! (p/eff! i [:rpc/call! [:rpc-fn/get-current-user]]))]
         (p/put! i [::set ::request got])
         (p/put! i [:db/got-entity (:user/user-id got) got])
         (p/put! i [:current-user/loaded]))))))



(mod/reg
 {:mod/name ::mod
  :mod/logic logic})