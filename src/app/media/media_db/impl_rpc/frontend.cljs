(ns app.media.media-db.impl-rpc.frontend
  (:require [clojure.core.async :refer [go <!]]
            [app.rpc.frontend :as rpc]
            [app.media.media-db.inter :as media-db]))

(defmethod media-db/query-result-chan! :media-db-impl/rpc [q]
  (go
    (let [res (<! (rpc/rpc-res-chan! [:rpc/media-db-query q]))]
      res)))
