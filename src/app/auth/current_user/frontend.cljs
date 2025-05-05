(ns app.auth.current-user.frontend
  (:require
   [app.frontend.mod :as mod]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]))

(defn- logic [i]
  (p/reg-reducer i ::set (fn [s [_ k v]] (assoc s k v)))

  (a/go
    (p/put! i [:current-user/hard-load]))

  (p/take-every!
   i :current-user/load
   (fn []
     (a/go
       (let [got (a/<! (p/eff! i [:rpc/send! [:rpc/get-current-user]]))]
         (p/put! i [::set ::current-user got])
         (p/put! i [:current-user/loaded])))))

  (p/take-every!
   i :current-user/hard-load
   (fn []
     (p/put! i [::set ::current-user result/loading])
     (p/put! i [:current-user/load]))))


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

(defn to-status [i]
  (cond
    (logged-in? i) :current-user/logged-in
    (logged-out? i) :current-user/logged-out
    :else :current-user/loading))

(defn to-current-user [i]
  (-> i ::current-user))

(mod/reg
 {:mod/name ::mod
  :mod/logic logic})