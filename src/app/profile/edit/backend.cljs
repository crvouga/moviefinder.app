(ns app.profile.edit.backend
  (:require
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]
   [app.user.user :as user]
   [app.user.user-db.inter :as user-db]
   [app.auth.session.session-db.inter :as session-db]))

(rpc/reg-fn
 :profile-edit/rpc
 (fn [ctx edits]
   (a/go
     (a/<! (a/timeout 1000))
     (let [session (a/<! (session-db/find-by-session-id! ctx (-> ctx :session/session-id)))
           user-existing (a/<! (user-db/find-by-user-id! ctx (:session/user-id session)))
           user-new (user/edit user-existing edits)]
       (a/<! (user-db/put! ctx user-new))))))
