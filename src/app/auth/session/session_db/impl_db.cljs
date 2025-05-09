(ns app.auth.session.session-db.impl-db
  (:require
   [app.auth.session.session-db.inter :as session-db]
   [clojure.core.async :as a]
   [clojure.set :refer [rename-keys]]
   [lib.db.inter :as db]
   [lib.posix :as posix]))



(defn- query-find-by-session-id [session-id]
  [:select [:session-id :user-id :created-at-posix :ended-at-posix]
   :from [:sessions]
   :where [:and
           [:= :session-id session-id]
           [:is :ended-at-posix nil]]
   :limit 1])

(defn- query-upsert [user-session]
  [:insert [:session-id :user-id :created-at-posix :ended-at-posix]
   :values (-> user-session
               (juxt :user-session/session-id
                     :user-session/user-id
                     :user-session/created-at
                     :user-session/ended-at))
   :on-conflict [:session-id]
   :do-update-set [:user-id :created-at-posix :ended-at-posix]])


(defn- query-delete [session-id]
  [:update [:sessions]
   :set [:ended-at-posix (posix/now!)]
   :where [:= :session-id session-id]])

(defn- row->user-session [row]
  (rename-keys row {:session-id :user-session/session-id
                    :user-id :user-session/user-id
                    :created-at-posix :user-session/created-at
                    :ended-at-posix :user-session/ended-at}))

(defn- query-result->user-sessions [query-result]
  (->> query-result
       :query-result/rows
       (map row->user-session)))

(defmethod session-db/init! :session-db/impl-db
  [config]
  config)

(defmethod session-db/find-by-session-id! :session-db/impl-db
  [{:keys [:db/db]} session-id]
  (a/go
    (let [q (query-find-by-session-id session-id)
          queried (a/<! (db/query! db q))
          user-sessions (query-result->user-sessions queried)]
      (first user-sessions))))


(defmethod session-db/put! :session-db/impl-db
  [{:keys [:db/db]} session]
  (db/query! db (query-upsert session)))

(defmethod session-db/zap! :session-db/impl-db
  [{:keys [:db/db]} session-id]
  (db/query! db (query-delete session-id)))
