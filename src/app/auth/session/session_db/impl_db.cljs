(ns app.auth.session.session-db.impl-db
  (:require
   [app.auth.session.session-db.interface :as session-db]
   [clojure.core.async :as a]
   [clojure.set :refer [rename-keys]]
   [core.db.interface :as db]))



(defn- query-find-by-session-id [session-id]
  [:select [:session-id :user-id :created-at-posix :ended-at-posix]
   :from [:sessions]
   :where [:and
           [:= :session-id session-id]
           [:is :ended-at-posix nil]]
   :limit 1])

(defn- query-upsert [user-session]
  [:insert [:session-id :user-id :created-at-posix :ended-at-posix]
   :values [(:user-session/session-id user-session)
            (:user-session/user-id user-session)
            (:user-session/created-at user-session)
            (:user-session/ended-at user-session)]
   :on-conflict [:session-id]
   :do-update-set [:user-id :created-at-posix :ended-at-posix]])

(defn now! []
  (.getTime (js/Date.)))

(defn- query-delete [session-id]
  [:update [:sessions]
   :set [:ended-at-posix (now!)]
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

(defmethod session-db/new! :session-db/impl-db
  [config]
  config)

(defmethod session-db/find-by-session-id :session-db/impl-db
  [{:keys [:db/db]} session-id]
  (a/go
    (let [q (query-find-by-session-id session-id)
          query-result (a/<! (db/query-chan! db q))
          user-sessions (query-result->user-sessions query-result)]
      (first user-sessions))))


(defmethod session-db/put! :session-db/impl-db
  [{:keys [:db/db]} session]
  (db/query-chan! db (query-upsert session)))

(defmethod session-db/zap! :session-db/impl-db
  [{:keys [:db/db]} session-id]
  (db/query-chan! db (query-delete session-id)))
