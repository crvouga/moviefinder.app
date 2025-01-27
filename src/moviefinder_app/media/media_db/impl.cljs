(ns moviefinder-app.media.media-db.impl
  (:require [clojure.core.async :refer [go]]
            [moviefinder-app.media.media-db.impl-fake.impl]
            [moviefinder-app.media.media-db.impl-tmdb-api.impl]
            [moviefinder-app.media.media-db.impl-rpc.impl]
            [moviefinder-app.media.media-db.interface :as media-db]))


(defmethod media-db/query-chan! :default [q]
  (go
    {:error/message "Media db implementation not found"
     :error/data q
     :query-result/query q
     :query-result/limit 25
     :query-result/offset 0
     :query-result/total 0
     :query-result/primary-key :media/id
     :query-result/rows []}))

