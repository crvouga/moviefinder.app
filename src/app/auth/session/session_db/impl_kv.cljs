(ns app.auth.session.session-db.impl-kv
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [lib.kv.inter :as kv]))


(defn new-by-session-id-kv [config]
  (kv/new! (merge config
                  {:kv/impl :kv/impl-namespaced
                   :kv/namespace ::by-session-id})))

(defmethod session-db/new! :session-db/impl-kv
  [config]
  (merge config
         {::by-session-id-kv (new-by-session-id-kv config)}))

(defmethod session-db/find-by-session-id! :session-db/impl-kv
  [{:keys [::by-session-id-kv]} session-id]
  (kv/get! by-session-id-kv session-id))

(defmethod session-db/put! :session-db/impl-kv
  [{:keys [::by-session-id-kv]} {:keys [:session/session-id] :as session}]
  (kv/set! by-session-id-kv session-id session))

(defmethod session-db/zap! :session-db/impl-kv
  [{:keys [::by-session-id-kv]} session-id]
  (kv/zap! by-session-id-kv session-id))
