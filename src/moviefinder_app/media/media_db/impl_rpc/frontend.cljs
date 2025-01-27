(ns moviefinder-app.media.media-db.impl-rpc.frontend
  (:require [clojure.core.async :refer [go <!]]
            [moviefinder-app.rpc.frontend :as rpc]
            [moviefinder-app.media.media-db.interface :as media-db]))

(defmethod media-db/query-chan! :media-db-impl/rpc [q]
  (go
    (let [result (<! (rpc/rpc-chan! [:rpc/media-db-query q]))]
      result)))
