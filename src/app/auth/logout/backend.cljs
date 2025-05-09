(ns app.auth.logout.backend
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [app.rpc.backend :as rpc]
   [clojure.core.async :as a]))

(rpc/reg-fn
 :rpc/logout
 (fn [ctx]
   (a/go
     (a/<! (session-db/zap! ctx (-> ctx :session/session-id))))))

