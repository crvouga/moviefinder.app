(ns app.media.media-db.impl-rpc.backend
  (:require [clojure.core.async :refer [<! go]]
            [app.media.media-db.impl-tmdb-api.impl]
            [app.media.media-db.inter :as media-db]
            [app.rpc.backend :as rpc]))

(defn update-impl [impl]
  (if (= impl :media-db/impl-rpc) :media-db/impl-tmdb-api impl))

(defn ensure-not-rpc-impl [req]
  (-> req
      (update :media-db/impl update-impl)))

(rpc/reg
 :media-db/rpc
 (fn [req]
   (go
     (let [res (<! (media-db/query! (-> req ensure-not-rpc-impl) req))]
       res))))
