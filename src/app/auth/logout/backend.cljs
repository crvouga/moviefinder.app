(ns app.auth.logout.backend
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]))

(rpc/reg
 :rpc/logout
 (fn [{:keys [session/session-id] :as req}]
   (a/go
     (a/<! (session-db/zap! req session-id)))))

