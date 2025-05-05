(ns app.profile.edit.backend
  (:require
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]
   [app.user.entity :as user]
   [app.user.user-db.inter :as user-db]
   [app.auth.session.session-db.inter :as session-db]))

(rpc/reg
 :profile-edit/rpc
 (fn [i]
   (a/go
     (println "update-current-user" i)
     (a/<! (a/timeout 1000))
     (let [session (a/<! (session-db/find-by-session-id! i (-> i :session/session-id)))
           user-existing (a/<! (user-db/find-by-user-id! i (:session/user-id session)))
           user-new (user/edit user-existing i)]
       (a/<! (user-db/put! i user-new))))))
