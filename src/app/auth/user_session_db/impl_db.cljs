(ns app.auth.user-session-db.impl-db
  (:require
   [app.auth.user-session-db.interface :as user-session-db]
   [clojure.core.async :as a]
   [clojure.set :refer [rename-keys]]
   [core.backend.db.interface :as db]))

(defmethod user-session-db/new! :user-session-db/impl-db
  [config]
  config)

(defn- query-find-by-session-id [session-id]
  [:select [:session-id :user-id :created-at-posix :ended-at-posix]
   :from [:user-sessions]
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
  [:update [:user-sessions]
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

(defmethod user-session-db/find-by-session-id :user-session-db/impl-db
  [{:keys [db]} session-id]
  (a/go
    (let [q (query-find-by-session-id session-id)
          query-result (a/<! (db/query-chan! db q))
          user-sessions (query-result->user-sessions query-result)]
      (first user-sessions))))


(defmethod user-session-db/put! :user-session-db/impl-db
  [{:keys [db]} user-session]
  (db/query-chan! db (query-upsert user-session)))

(defmethod user-session-db/zap! :user-session-db/impl-db
  [{:keys [db]} session-id]
  (db/query-chan! db (query-delete session-id)))
