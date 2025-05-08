(ns app.feed.feed-db.impl-kv
  (:require
   [app.feed.feed :as feed]
   [app.feed.feed-db.inter :as feed-db]
   [lib.kv.inter :as kv]))

(defn- new-feed-kv [kv]
  (->> {:kv/namespace ::feed} (merge kv) kv/new!))

(defmethod feed-db/new! :feed-db/impl-kv
  [i] (-> i (assoc :feed-db/feed-kv (new-feed-kv i))))

(defmethod feed-db/get! :feed-db/impl-kv
  [{:keys [feed-db/feed-kv]} feed-id]
  (kv/get! feed-kv feed-id))

(defmethod feed-db/put! :feed-db/impl-kv
  [{:keys [feed-db/feed-kv]} feed]
  (kv/set! feed-kv (feed/id feed) feed))
