(ns app.media.media-db.impl-rpc.frontend
  (:require [clojure.core.async :refer [<! go]]
            [app.rpc.frontend :as rpc]
            [app.media.media-db.inter :as media-db]))

(defmethod media-db/query! :media-db/impl-rpc [_inst q]
  (go
    (let [q (assoc q :media-db/impl :media-db/impl-tmdb-api)
          res (<! (rpc/rpc-res-chan! [:media-db/rpc q]))]
      res)))
