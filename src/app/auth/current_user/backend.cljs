(ns app.auth.current-user.backend
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [app.user.user-db.inter :as user-db]
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]))

(defmethod rpc/rpc! :rpc/get-current-user [[_ req]]
  (a/go
    (let [session-id (:session/id req)
          session (a/<! (session-db/find-by-session-id! req session-id))
          user-id (:user/id session)
          user (a/<! (user-db/find-by-id! req user-id))]
      user)))
