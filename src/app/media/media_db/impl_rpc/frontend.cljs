(ns app.media.media-db.impl-rpc.frontend
  (:require [clojure.core.async :as a]
            [app.rpc.frontend :as rpc]
            [app.media.media-db.inter :as media-db]))

(defmethod media-db/query! :media-db/impl-rpc [_inst q]
  (a/go (let [res (a/<! (rpc/rpc-res-chan! [:rpc/media-db-query (assoc q :media-db/impl :media-db/impl-tmdb-api)]))]
          res)))
