(ns app.feed.feed-db.inter
  (:require [clojure.core.async :refer [go <!]]
            [app.feed.feed :as feed]))

(defmulti init :feed-db/impl)
(defmulti get! :feed-db/impl)
(defmulti put! :feed-db/impl)

(defn get-else-default! [inst feed-id]
  (go
    (let [feed (<! (get! inst feed-id))]
      (if (feed/valid? feed)
        feed
        (feed/default)))))