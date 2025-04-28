(ns app.auth.user-session-db.impl-kv
  (:require
   [app.auth.user-session-db.interface :as user-session-db]
   [clojure.core.async :as a]
   [core.kv.interface :as kv]))

(defmethod user-session-db/new! :user-session-db/impl-kv
  [config]
  (a/go
    (merge config
           {::by-session-id-kv (a/<! (kv/new! {:kv/impl :kv/impl-atom}))})))

(defmethod user-session-db/find-by-session-id :user-session-db/impl-kv
  [{:keys [::by-session-id-kv]} session-id]
  (kv/get! by-session-id-kv session-id))

(defmethod user-session-db/put! :user-session-db/impl-kv
  [{:keys [::by-session-id-kv]} {:keys [:user-session/session-id] :as user-session}]
  (kv/set! by-session-id-kv session-id user-session))

(defmethod user-session-db/zap! :user-session-db/impl-kv
  [{:keys [::by-session-id-kv]} session-id]
  (kv/zap! by-session-id-kv session-id))
