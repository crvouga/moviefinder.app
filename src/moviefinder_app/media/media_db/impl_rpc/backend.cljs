(ns moviefinder-app.media.media-db.impl-rpc.backend
  (:require [clojure.core.async :refer [<! go]]
            [moviefinder-app.media.media-db.impl-tmdb-api.impl]
            [moviefinder-app.media.media-db.interface :as media-db]
            [moviefinder-app.rpc.backend :as rpc]))

(defn update-impl [impl]
  (if (= impl :media-db-impl/rpc) :media-db-impl/tmdb-api impl))

(defn ensure-not-rpc-impl [req]
  (-> req
      (update :media-db/impl update-impl)))

(defmethod rpc/rpc! :rpc/media-db-query [req]
  (go
    (let [q (-> req second ensure-not-rpc-impl)
          res (<! (media-db/query-result-chan! q))]
      res)))
        