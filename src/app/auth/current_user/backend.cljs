(ns app.auth.current-user.backend
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [app.user.user-db.inter :as user-db]
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]))

(rpc/reg
 :rpc/get-current-user
 (fn [{:keys [session/session-id] :as req}]
   (a/go
     (a/<! (a/timeout 1000))
     (let [{:keys [session/user-id]} (a/<! (session-db/find-by-session-id! req session-id))]
       (a/<! (user-db/find-by-user-id! req user-id))))))

