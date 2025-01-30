(ns moviefinder-app.media.media-db.impl-db-conn.impl
  (:require
   [moviefinder-app.media.media-db.interface :as media-db]
   [cljs.core.async :refer [go]]))

(def migrations
  [[:create-table [:media :if-not-exists]
    :with-columns [[:id :text :primary-key]
                   [:title :text]
                   [:release-date :text]
                   [:overview :text]
                   [:poster-path :text]
                   [:vote-average :real]
                   [:vote-count :integer]
                   [:popularity :real]
                   [:poster-url :text]
                   [:backdrop-url :text]]]])



(defmethod media-db/put-chan! :media-db-impl/db-conn
  [_i]
  (go
    {:media-db/impl :media-db-impl/db-conn
     :result/type :result/ok}))


(defmethod media-db/query-result-chan! :media-db-impl/db-conn
  [_i]
  (go
    media-db/empty-query-result))





